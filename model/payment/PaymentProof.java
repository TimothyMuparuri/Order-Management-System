package za.co.nharire.order_ms.model.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@Entity
@Table(name= "paymentproof")

public class PaymentProof {

    @Id
    private Integer proofId;
    private Integer orderId;
    private String filePath;
    private Boolean verified;

}
