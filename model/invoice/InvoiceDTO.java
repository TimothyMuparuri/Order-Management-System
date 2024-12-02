package za.co.nharire.order_ms.model.invoice;

import lombok.Data;
import org.apache.poi.hpsf.Decimal;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InvoiceDTO implements Serializable {

    private Integer invoiceId;
    private Integer orderId;
    private LocalDateTime invoiceDate;
    private Decimal totalAmount;
    private Decimal paidAmount;
    private Decimal remainingBalance;
    private String invoicePdfUrl;
}
