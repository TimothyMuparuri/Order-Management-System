package za.co.nharire.order_ms.model.order;

import lombok.Data;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.apache.poi.hpsf.Decimal;

import java.io.Serializable;

@Data
public class OrderDTO implements Serializable {
    private Integer orderId;
    private Integer userId;
    private String productDetails;
    private String orderStatus;
    private Boolean depositPaid;
    private Decimal depositAmount;
    private Decimal remainingBalance;
    private String paymentProof;
    private DateTime orderDate;
}
