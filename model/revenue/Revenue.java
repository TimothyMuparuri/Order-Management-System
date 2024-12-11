package za.co.nharire.order_ms.model.revenue;

import jakarta.persistence.*;
import lombok.Data;
import org.apache.james.mime4j.dom.datetime.DateTime;
import org.apache.poi.hpsf.Decimal;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "revenue")

public class Revenue {

    @Id
    @Column(name= "reportId")
    private Integer reportId;
    @Column(name= "weekStartDate")
    private LocalDateTime weekStartDate;
    @Column(name= "weekEndDate")
    private LocalDateTime weekEndDate;
    @Column(name= "totalRevenue")
    private Double totalRevenue;
    @Column(name= "totalCommission")
    private Double totalCommission;
    @Column(name= "reportGeneratedAt")
    private LocalDateTime reportGeneratedAt;
}
