package za.co.nharire.order_ms.service;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import za.co.nharire.order_ms.model.order.Order;
import za.co.nharire.order_ms.model.revenue.Revenue;
import za.co.nharire.order_ms.model.revenue.RevenueDTO;
import za.co.nharire.order_ms.repository.OrderRepository;
import za.co.nharire.order_ms.repository.RevenueRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class RevenueService {

    private final RevenueRepository revenueRepository;
//    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final OrderRepository orderRepository;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer reportId = 0;

//    @Scheduled(fixedRate = 60800000L)
    public void saveRevenue() {

        Revenue revenue = new Revenue();
//        BeanUtils.copyProperties(revenueDTO, revenue);

        List<Order> orderList = orderRepository.findAll();
        double totalRevenue = 0;
        if (orderList.isEmpty()) {
            log.error("there are no orders for this week");
        } else {
            for (Order order : orderList) {
                if (order.getTotalCost().equals(null)) {
                    totalRevenue = 0;
                } else {
                    totalRevenue += order.getTotalCost();
                }
            }
        }

        log.info(" Save Revenue to DB");
        revenue.setReportId(reportId);
        revenue.setTotalRevenue(totalRevenue);
        revenue.setTotalCommission(0.3 * totalRevenue);
        revenue.setReportGeneratedAt(LocalDateTime.now());
        LocalDateTime weekEndDate = LocalDateTime.now();
        revenue.setWeekEndDate(weekEndDate);
        revenue.setWeekStartDate(weekEndDate.minusWeeks(1));
        revenueRepository.save(revenue);
        reportId++;

//        BeanUtils.copyProperties(revenue1, revenueDTO);
//        return revenueDTO;
    }

    public List<RevenueDTO> getALlRevenue() {

        log.info("IN SERVICE ");

        List<RevenueDTO> revenueDTOList = new ArrayList<>();

        List<Revenue> revenueList = revenueRepository.findAll();
        for (Revenue revenue : revenueList) {
            RevenueDTO revenueDTO = new RevenueDTO();
            BeanUtils.copyProperties(revenue, revenueDTO);
            revenueDTOList.add(revenueDTO);
        }
        return revenueDTOList;
    }
}


