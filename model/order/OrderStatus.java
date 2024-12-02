package za.co.nharire.order_ms.model.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orderstatus")

public class OrderStatus {

    @Id
    @Column(name = "statusId")
    private Integer statusId;
    @Column(name = "orderId")
    private Integer orderId;
    @Column(name = "status")
    private String status;
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
