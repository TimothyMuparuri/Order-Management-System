package za.co.nharire.order_ms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import za.co.nharire.order_ms.model.order.OrderStatus;
import za.co.nharire.order_ms.model.order.OrderStatusDTO;
import za.co.nharire.order_ms.repository.OrderStatusRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j

public class OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;

    public OrderStatusDTO saveOrderStatus(OrderStatusDTO orderStatusDTO) {

        OrderStatus orderStatus = new OrderStatus();
        BeanUtils.copyProperties(orderStatusDTO, orderStatus);

        log.info(" Save OrderStatus to DB");
        OrderStatus orderStatus1 = orderStatusRepository.save(orderStatus);

        BeanUtils.copyProperties(orderStatus1, orderStatusDTO);
        return orderStatusDTO;
    }

    public List<OrderStatusDTO> getALlOrdersStatus() {

        log.info("IN SERVICE ");

        List<OrderStatusDTO> orderStatusDTOList = new ArrayList<>();

        List<OrderStatus> orderStatusList = orderStatusRepository.findAll();
        for (OrderStatus orderStatus : orderStatusList) {
            OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
            BeanUtils.copyProperties(orderStatusDTO, orderStatusDTO);
            orderStatusDTOList.add(orderStatusDTO);
        }
        return orderStatusDTOList;
    }

    public OrderStatusDTO getOrderStatusById(Integer statusId) {
        log.info("IN SERVICE ");

        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        Optional<OrderStatus> orderStatus = orderStatusRepository.findById(statusId);

        if (orderStatus.isEmpty()) {
            return null;
        } else {
            BeanUtils.copyProperties(orderStatus.get(), orderStatusDTO);
            return orderStatusDTO;
        }
    }

    public OrderStatusDTO updateOrderStatus(Integer statusId, OrderStatusDTO orderStatusDTODetails) {

        log.info("IN SERVICE ");
        OrderStatusDTO orderStatusDTO = new OrderStatusDTO();
        Optional<OrderStatus> orderStatus = orderStatusRepository.findById(statusId);

        if (orderStatus.isEmpty()) {
            return null;
        } else {

            OrderStatus orderStatus1 = orderStatus.get();
            orderStatus1.setStatus(orderStatusDTO.getStatus());
            return orderStatusDTO;
        }

    }
}
