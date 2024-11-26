package za.co.nharire.order_ms.model.revenue;

import lombok.Data;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.apache.poi.hpsf.Decimal;

import java.io.Serializable;

@Data
public class RevenueDTO implements Serializable {

    private Integer reportId;
    private DateTime weekStartDate;
    private DateTime weekEndDate;
    private Decimal totalRevenue;
    private Decimal totalCommission;
    private DateTime reportGeneratedAt;
}
