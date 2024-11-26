package za.co.nharire.order_ms.model.invoice;

import lombok.Data;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.apache.poi.hpsf.Decimal;

import java.io.Serializable;

@Data
public class InvoiceDTO implements Serializable {

    private Integer invoiceId;
    private Integer orderId;
    private DateTime invoiceDate;
    private Decimal totalAmount;
    private Decimal paidAmount;
    private Decimal remainingBalance;
    private String invoicePdfUrl;
}
