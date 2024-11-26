package za.co.nharire.order_ms.model.order;

import lombok.Data;
import org.apache.james.mime4j.dom.datetime.DateTime;

import java.io.Serializable;

@Data
public class OrderStatusDTO implements Serializable {

    private Integer statusId;
    private Integer orderId;
    private String status;
    private DateTime updatedAt;

}
