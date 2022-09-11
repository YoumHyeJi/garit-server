package com.garit.study.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 화면이나 API에 의존하는 쿼리는 해당 클래스에서 생성한다.
 * => 핵심 비즈니스 로직(순수 엔티티 조회 = OrderRepository)의 라이프 사이클과,
 * => 화면에 의존적인 로직의 라이프 사이클은 많이 다르다.
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        /**
         * Order 조회 => 2개 조회됨
         */
        List<OrderQueryDto> result = findOrders();  // query 1번 실행 => 결과 N개

        /**
         * 루프를 돌면서, 컬랙션 부분(orderItems) 채우기
         */
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());    // query N번 실행
            o.setOrderItems(orderItems);
        });

        return result;
    }

    /**
     * in 쿼리를 사용해서 OrderItem 리스트를 한번에 DB에서 가져온다.
     */
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders();  // query 1번

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));      // query 1번

        /**
         * 메모리에서 order 별로 orderItem을 매칭해준다.
         * 즉, 컬랙션 채우기
         */
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        // 쿼리를 한번만 날려서 OrderItemQueryDto 리스트를 한방에 가져온다.
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new com.garit.study.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        /**
         * orderId를 기준으로 map으로 바꾸기
         * key : orderId    ,    value : List<OrderItemQueryDto>>
         */
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    @NotNull
    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "select new com.garit.study.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();

    }

    /**
     * new 오퍼레이션을 사용할 때, 데이터를 플랫하게 한줄밖에 못 넣는다.
     * 따라서 컬랙션을 파라미터로 바로 넣을 수 없다.
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "select new com.garit.study.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }


    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new" +
                        " com.garit.study.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"+
                        " from Order o"+
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();

    }
}
