package za.co.nharire.order_ms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import za.co.nharire.order_ms.constants.ApiConstants;
import za.co.nharire.order_ms.model.delete.DeleteDTO;
import za.co.nharire.order_ms.model.order.Order;
import za.co.nharire.order_ms.model.order.OrderDTO;
import za.co.nharire.order_ms.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class OrderService {

    private OrderRepository orderRepository;

    public OrderDTO saveOrder(OrderDTO orderDTO) {

        Order order = new Order();
        BeanUtils.copyProperties(orderDTO, order);

        log.info(" Save Order to DB");
        Order order1 = orderRepository.save(order);

        BeanUtils.copyProperties(order1, orderDTO);
        return orderDTO;
    }

    public List<OrderDTO> getALlOrders() {

        log.info("IN SERVICE ");

        List<OrderDTO> orderDTOList = new ArrayList<>();

        List<Order> orderList = orderRepository.findAll();
        for (Order order : orderList) {
            OrderDTO orderDTO = new OrderDTO();
            BeanUtils.copyProperties(order, orderDTO);
            orderDTOList.add(orderDTO);
        }
        return orderDTOList;
    }

    public OrderDTO getOrderById(Integer orderId) {
        log.info("IN SERVICE ");

        OrderDTO orderDTO = new OrderDTO();
        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isEmpty()) {
            return null;
        } else {
            BeanUtils.copyProperties(order.get(), orderDTO);
            return orderDTO;
        }
    }

    public OrderDTO updateOrder(Integer orderId, OrderDTO orderDTODetails) {

        log.info("IN SERVICE ");
        OrderDTO orderDTO = new OrderDTO();
        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isEmpty()) {
            return null;
        } else {

            Order order1 = order.get();
            order1.setOrderDate(orderDTO.getOrderDate());
            order1.setOrderStatus(orderDTO.getOrderStatus());
            return orderDTO;
        }

    }

    public DeleteDTO deleteOrder(Integer orderId) {
        log.info("IN SERVICE ");

        DeleteDTO orderDTO = new DeleteDTO();

        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            log.error(" No OrderId found for this ID " + orderId);

            orderDTO.setMessage(ApiConstants.DELETE_FAILURE + orderId);
            return orderDTO;
        } else {
            orderRepository.deleteById(orderId);
        }
        orderDTO.setMessage(ApiConstants.DELETE_SUCCESS);
        return orderDTO;
    }

}



