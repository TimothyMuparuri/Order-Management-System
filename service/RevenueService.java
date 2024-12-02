package za.co.nharire.order_ms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import za.co.nharire.order_ms.model.revenue.Revenue;
import za.co.nharire.order_ms.model.revenue.RevenueDTO;
import za.co.nharire.order_ms.repository.RevenueRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class RevenueService {

    private final RevenueRepository revenueRepository;

    public RevenueDTO saveRevenue(RevenueDTO revenueDTO) {

        Revenue revenue = new Revenue();
        BeanUtils.copyProperties(revenueDTO, revenue);

        log.info(" Save Revenue to DB");
        Revenue revenue1 = revenueRepository.save(revenue);

        BeanUtils.copyProperties(revenue1, revenueDTO);
        return revenueDTO;
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


