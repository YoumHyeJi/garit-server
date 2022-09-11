package com.garit.study.repository.order.simplequery;

import com.garit.study.domain.Address;
import com.garit.study.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Repository에서 Controller로 의존관계가 생길 수 있기 때문에,
 * DTO 클래스를 별도로 만들어준다.
 */
@Data
public class OrderSimpleQueryDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    /**
     * DTO는 엔티티를 참조해도 괜찮다.
     */
    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }

}
