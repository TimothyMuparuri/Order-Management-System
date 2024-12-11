package za.co.nharire.order_ms.model.payment;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "paymentproof")

public class PaymentProof {

    @Id
    @Column(name = "proofId")
    private Integer proofId;
    @Column(name = "orderId")
    private Integer orderId;
    @Column(name = "filePath")
    private String filePath;
    @Column(name = "verified")
    private Boolean verified;

}
