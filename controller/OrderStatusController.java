package za.co.nharire.order_ms.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.nharire.order_ms.model.order.OrderDTO;
import za.co.nharire.order_ms.model.order.OrderStatus;
import za.co.nharire.order_ms.model.order.OrderStatusDTO;
import za.co.nharire.order_ms.service.OrderStatusService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j

public class OrderStatusController {

    private OrderStatusService orderStatusService;

    @PostMapping("orderstatus/save")
    public ResponseEntity<OrderStatusDTO> saveOrder(@RequestBody OrderStatusDTO orderStatusDTO) {

        log.info("SAVING ORDERSTATUS", orderStatusDTO.toString());

        OrderStatusDTO orderStatusDTO1 = orderStatusService.saveOrderStatus(orderStatusDTO);
        if (orderStatusDTO1 != null) {
            return new ResponseEntity<>(orderStatusDTO1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orderstatus/all")
    public ResponseEntity<List<OrderStatusDTO>> getAllOrdersStatus() {
        log.info("GETTING ALL ORDERSSTATUS IN DB ");

        List<OrderStatusDTO> orderStatusDTOList = orderStatusService.getALlOrdersStatus();

        if (orderStatusDTOList != null) {
            return new ResponseEntity<>(orderStatusDTOList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/orderstatus/get/{statusId}")
    public ResponseEntity<OrderStatusDTO> getOrderById(@PathVariable Integer statusId) {
        log.info("GETTING A ORDERSTATUS IN DB ");

        OrderStatusDTO orderStatusDTO = orderStatusService.getOrderStatusById(statusId);

        if (orderStatusDTO != null) {
            return new ResponseEntity<>(orderStatusDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/orderstatus/update/{statusId}")
    public ResponseEntity<OrderStatusDTO > updateOrder(@PathVariable Integer statusId, @RequestBody OrderStatusDTO orderStatusDTO) {
        log.info("UPDATING A OrderStatus IN DB ");

        OrderStatusDTO orderStatusDTO1 = orderStatusService.updateOrderStatus(statusId, orderStatusDTO);

        if (orderStatusDTO1 != null) {
            return new ResponseEntity<>(orderStatusDTO1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
