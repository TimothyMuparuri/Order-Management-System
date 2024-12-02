package za.co.nharire.order_ms.model.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "_order")

public class Order {

    @Id
    @Column(name = "orderId")
    private Integer orderId;
    @Column(name = "userId")
    private Integer userId;
    @Column(name = "productDetails")
    private String productDetails;
    @Column(name = "orderStatus")
    private String orderStatus;
    @Column(name = "depositPaid")
    private Boolean depositPaid;
    @Column(name = "depositAmount")
    private Double depositAmount;
    @Column(name = "remainingBalance")
    private Double remainingBalance;
    @Column(name = "paymentProof")
    private String paymentProof;
    @Column(name = "orderDate")
    private LocalDateTime orderDate;


}
