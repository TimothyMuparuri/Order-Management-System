package za.co.nharire.order_ms.model.revenue;

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
@Table(name = "revenue")

public class Revenue {

    @Id
    private Integer reportId;
    private DateTime weekStartDate;
    private DateTime weekEndDate;
    private Decimal totalRevenue;
    private Decimal totalCommission;
    private DateTime reportGeneratedAt;
}
