package za.co.nharire.order_ms.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.nharire.order_ms.constants.ApiConstants;
import za.co.nharire.order_ms.model.delete.DeleteDTO;
import za.co.nharire.order_ms.model.order.OrderDTO;
import za.co.nharire.order_ms.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j

public class OrderController {

    private OrderService orderService;

    @PostMapping("order/save")
    public ResponseEntity<OrderDTO> saveOrder(@RequestBody OrderDTO orderDTO) {

        log.info("SAVING ORDER", orderDTO.toString());

        OrderDTO orderDTO1 = orderService.saveOrder(orderDTO);
        if (orderDTO1 != null) {
            return new ResponseEntity<>(orderDTO1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/order/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        log.info("GETTING ALL ORDERS IN DB ");

        List<OrderDTO> orderDTOList = orderService.getALlOrders();

        if (orderDTOList != null) {
            return new ResponseEntity<>(orderDTOList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/order/get/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer orderId) {
        log.info("GETTING A ORDER IN DB ");

        OrderDTO orderDTO = orderService.getOrderById(orderId);

        if (orderDTO != null) {
            return new ResponseEntity<>(orderDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/order/update/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Integer orderId, @RequestBody OrderDTO orderDTO) {
        log.info("UPDATING A Order IN DB ");

        OrderDTO orderDTO1 = orderService.updateOrder(orderId, orderDTO);

        if (orderDTO1 != null) {
            return new ResponseEntity<>(orderDTO1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/order/delete/{orderId}")
    public ResponseEntity<DeleteDTO> deleteOrder(@PathVariable Integer orderId) {
        log.info("DELETING AN ORDER IN DB ");

        DeleteDTO order = orderService.deleteOrder(orderId);

        if (order.getMessage().equals(ApiConstants.DELETE_SUCCESS)) {
            return new ResponseEntity<>(order, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(order, HttpStatus.NOT_FOUND);
        }
    }
}
