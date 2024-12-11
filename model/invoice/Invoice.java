package za.co.nharire.order_ms.model.invoice;

import jakarta.persistence.*;
import lombok.Data;
import org.apache.poi.hpsf.Decimal;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invoice")

public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "invoiceId")
    private Integer invoiceId;
    @Column(name= "orderId")
    private Integer orderId;
    @Column(name= "invoiceDate")
    private LocalDateTime invoiceDate;
    @Column(name= "totalAmount")
    private Double totalAmount;
    @Column(name= "paidAmount")
    private Double paidAmount;
    @Column(name= "remainingBalance")
    private Double remainingBalance;
    @Column(name= "invoicePdfUrl")
    private String invoicePdfUrl;
}
