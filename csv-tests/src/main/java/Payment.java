import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Created by Alexander on 04.11.2017.
 */

@ToString @EqualsAndHashCode @Accessors(chain = true) @Getter @Setter
@JsonPropertyOrder(value = {"user_id", "current_date", "is_paid", "monthly_price", "product_group"})
@JsonRootName("test_task_for_QA.csv")
public class Payment implements Cloneable {

    @JsonFormat(shape = JsonFormat.Shape.NUMBER) private long user_id;
    @JsonFormat(shape = JsonFormat.Shape.OBJECT) private Date current_date;
    @JsonFormat(shape = JsonFormat.Shape.STRING) private String is_paid;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT) private double monthly_price;
    @JsonFormat(shape = JsonFormat.Shape.STRING) private String product_group;

    @Override
    protected Payment clone() {
        try {
            return (Payment) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new Payment();
    }
}
