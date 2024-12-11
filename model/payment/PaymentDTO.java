package za.co.nharire.order_ms.model.payment;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentDTO implements Serializable {

    private Integer proofId;
    private Integer orderId;
    private String filePath;
    private Boolean verified;

}
