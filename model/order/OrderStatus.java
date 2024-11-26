package za.co.nharire.order_ms.model.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.james.mime4j.dom.datetime.DateTime;

@Data
@Builder
@RequiredArgsConstructor
@Entity
@Table(name = "orderstatus")

public class OrderStatus {

    @Id
    private Integer statusId;
    private Integer orderId;
    private String status;
    private DateTime  updatedAt;
}
