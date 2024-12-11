package za.co.nharire.order_ms.model.order;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orderstatus")

public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statusId")
    private Integer statusId;
    @Column(name = "orderId")
    private Integer orderId;
    @Column(name = "status")
    private String status;
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;
}
