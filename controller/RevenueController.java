package za.co.nharire.order_ms.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import za.co.nharire.order_ms.model.revenue.RevenueDTO;
import za.co.nharire.order_ms.service.RevenueService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j

public class RevenueController {

    private RevenueService revenueService;

    @PostMapping("revenue/save")
    public ResponseEntity<RevenueDTO> saveRevenue(@RequestBody RevenueDTO revenueDTO) {

        log.info("SAVING REVENUE", revenueDTO.toString());

        RevenueDTO revenueDTO1 = revenueService.saveRevenue(revenueDTO);
        if (revenueDTO1 != null) {
            return new ResponseEntity<>(revenueDTO1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/revenue/all")
    public ResponseEntity<List<RevenueDTO>> getAllRevenues() {
        log.info("GETTING ALL REVENUES IN DB ");

        List<RevenueDTO> revenueDTOList = revenueService.getALlRevenue();

        if (revenueDTOList != null) {
            return new ResponseEntity<>(revenueDTOList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
