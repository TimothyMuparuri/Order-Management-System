package za.co.nharire.order_ms.model.order;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OrderDTO implements Serializable {
    private Integer orderId;
    private Integer userId;
    private String productDetails;
    private String orderStatus;
    private Boolean depositPaid;
    private Double depositAmount;
    private Double remainingBalance;
    private String paymentProof;
    private LocalDateTime orderDate = LocalDateTime.now() ;
}
