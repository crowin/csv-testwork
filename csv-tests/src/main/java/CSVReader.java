import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by Alexander on 04.11.2017.
 */
public class CSVReader {
    private Payment lastPayment;
    private Payment currentPayment;
    private Map<Payment, String> badPaymentsMap;
    //пусть будет список всех групп продуктов
    private List<String> productGroups = Arrays.asList("AAA", "BBB", "CCC", "DDD");

    /**
     * Есть два списка current и last. lastPayment необходим для сверки даты. currentPayment нужен для всех проверок
     * Итог: вывод в консоль кривых записей и описания ошибок
     * @throws IOException
     */
    public void printErrors() throws IOException {
        badPaymentsMap = new HashMap<>();
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.typedSchemaFor(Payment.class).withHeader();
        File csv = new File("src/main/resources/test_task_for_QA.csv");
        MappingIterator<Payment> it = mapper.
                setDateFormat(new SimpleDateFormat("yyyy-MM-dd")).
                readerFor(Payment.class).
                with(schema.withColumnSeparator(CsvSchema.DEFAULT_COLUMN_SEPARATOR)).
                readValues(csv);
        while (it.hasNext()) {
            try {
                currentPayment = it.next();
            } catch (RuntimeJsonMappingException ex) {
                badPaymentsMap.put(new Payment().setCurrent_date(new Date()), ex.getCause().getLocalizedMessage());
                lastPayment = null;
                continue;
            }
            validRecord();
            lastPayment = currentPayment.clone();
        }
        for(Map.Entry<Payment, String> entry : badPaymentsMap.entrySet()) {
            Payment key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key);
            System.out.println(value);
            System.out.println("--------------------------------");
        }
        System.out.println("--------------------------------");
        System.out.println("Errors quantity: " +badPaymentsMap.size()+ "");
    }

    /**
     * StringBuilder errorMsg соеденяет все ошибки, т.е если в одной записи более одной ошибки -
     * они будут объеденены в одну строку
     */
    private void validRecord() {
        StringBuilder errorMsg = new StringBuilder().append("");
        //Если дата не установлена
        if (currentPayment.getCurrent_date() == null || currentPayment.getCurrent_date().getTime() == 0){
            errorMsg.append("current_date wasn't set; ");
        }
        //Если isPaid <> true или false
        if (!currentPayment.getIs_paid().equals("TRUE") && !currentPayment.getIs_paid().equals("FALSE")) {
            errorMsg.append("is_payment contains bad value: ").append(currentPayment.getIs_paid()).append("; ");
        }
        //Если isPaid == true и прайс равен или меньшя нуля  или наоборот
        if ((currentPayment.getIs_paid().equals("TRUE") && currentPayment.getMonthly_price() <=0) ||
                (currentPayment.getIs_paid().equals("FALSE") && currentPayment.getMonthly_price() != 0)) {
            errorMsg.append("monthly_price contains incorrect value for isPaid = ").append(currentPayment.getIs_paid()).append("; ");
        }
        //Если продукт не входит в группу продуктов
        if (!productGroups.contains(currentPayment.getProduct_group())) {
            errorMsg.append("product_group mustn't include this product; ");
        }
        //Если следующая дата за прошлой записью больше чем на один день или меньше прошлой
        if (getDifferenceBetweenDays() > 1 || getDifferenceBetweenDays() < 0) {
            errorMsg.append("difference between current record ").append("and last record (from ").
                    append(lastPayment.getCurrent_date()).
            append(") is ").append(getDifferenceBetweenDays()).append(" days");
        }
        //Если текущая запись дублирует прошлую
        if (lastPayment != null && currentPayment.equals(lastPayment)) {
            errorMsg.append("current record is duplicate of past record");
        }
        if (!errorMsg.toString().equals("")) {
            badPaymentsMap.put(currentPayment, errorMsg.toString());
        }
    }

    private long getDifferenceBetweenDays() {
        if (lastPayment == null) {
            return 0;
        }
        else if (currentPayment.getCurrent_date().getTime() != lastPayment.getCurrent_date().getTime()) {
            return ChronoUnit.DAYS.between(
                    lastPayment.getCurrent_date().toInstant(), currentPayment.getCurrent_date().toInstant());
        } else return 0;
    }

    public static void main(String[] args) throws IOException {
        CSVReader reader = new CSVReader();
        reader.printErrors();
    }
}
