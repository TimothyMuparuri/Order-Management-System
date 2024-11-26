package za.co.nharire.order_ms.model.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.apache.poi.hpsf.Decimal;

@Data
@Builder
@RequiredArgsConstructor
@Entity
@Table(name = "order")

public class Order {

    @Id
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
