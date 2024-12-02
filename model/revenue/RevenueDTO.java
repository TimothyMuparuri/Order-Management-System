package za.co.nharire.order_ms.model.revenue;

import lombok.Data;
import org.apache.poi.hpsf.Decimal;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RevenueDTO implements Serializable {

    private Integer reportId;
    private LocalDateTime weekStartDate;
    private LocalDateTime weekEndDate;
    private Decimal totalRevenue;
    private Decimal totalCommission;
    private LocalDateTime reportGeneratedAt;
}
