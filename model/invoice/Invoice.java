package za.co.nharire.order_ms.model.invoice;

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
@Table(name = "invoice")

public class Invoice {

    @Id
    private Integer invoiceId;
    private Integer orderId;
    private DateTime invoiceDate;
    private Decimal totalAmount;
    private Decimal paidAmount;
    private Decimal remainingBalance;
    private String invoicePdfUrl;
}
