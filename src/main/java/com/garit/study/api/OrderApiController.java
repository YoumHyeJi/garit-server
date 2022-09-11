package com.garit.study.api;

import com.garit.study.domain.Address;
import com.garit.study.domain.Order;
import com.garit.study.domain.OrderItem;
import com.garit.study.domain.OrderStatus;
import com.garit.study.repository.OrderRepository;
import com.garit.study.repository.OrderSearch;
import com.garit.study.repository.order.query.OrderFlatDto;
import com.garit.study.repository.order.query.OrderItemQueryDto;
import com.garit.study.repository.order.query.OrderQueryDto;
import com.garit.study.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;


    /**
     * V1 : 엔티티를 직접 노출
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        /**
         * Hibernate5Module 기본설정을 사용하고 있다.
         * LAZY LOADING을 통해 프록시 초기화돼서, 데이터가 로딩된 객체만 반환이 된다.
         * 따라서 객체 그래프를 초기화할 필요가 있다.
         */
        for (Order order : all) {
            // Member 프록시 초기화
            order.getMember().getName();

            // Delivery 프록시 초기화
            order.getDelivery().getAddress();

            // OrderItem 프록시 초기화
            List<OrderItem> orderItems = order.getOrderItems();

            // Item 프록시 초기화
            orderItems.stream().forEach(oi -> oi.getItem().getName());
        }

        return all;
    }

    /**
     * V2 : 엔티티를 DTO로 변환
     * => 모든 엔티티 (Order, Member, Delivery, OrderItems, Item)의 스펙이 외부로 노출되면 안된다.
     * => 따라서 모든 엔티티를 DTO로 변환해야 한다.
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * V3 : 페치 조인 최적화
     * <p>
     * [장점]
     * 1. 페치 조인으로 SQL이 1번만 실행됨 (쿼리가 적게 나감)
     * <p>
     * <p>
     * [문제점]
     * 1. 1:N 연관 관계에서 페치 조인을 하면, 데이터 양이 N만큼 증가한다. (네트워크 용량이 많아짐)
     * => 1:N 조인이 있으므로 데이터베이스 row가 증가한다.
     * => 그결과 같은 order 엔티티의 조회 수도 증가하게 된다.
     * <p>
     * => JPA의 distinct는 SQL에 distinct를 추가하고, 같은 엔티티가 조회되면 애플리케이션에서 중복을 걸러준다.
     * => 즉, distinct 키워드를 사용해서 중복 조회를 걸러준다.
     * <p>
     * <p>
     * 2. 페이징 불가능
     * => 컬랙션 페치 조인에서는 페이징 처리를 할 수 없기 때문에, 메모리에서 페이징 처리를 해버린다.
     * => 만약 데이터가 10000개가 있었으면, 10000개를 모두 메모리에 올린 다음에, 페이징 처리를 해야 한다. (outOfMemory 발생할 수도..)
     * <p>
     * => 컬랙션 페치 조인의 결과로 데이터가 N만큼 증가하기 때문에, Order를 기준으로 정확한 페이징 처리가 불가능해진다.
     * <p>
     * <p>
     * 3. 컬랙션 페치 조인은 1개만 사용할 수 있따.
     * => 컬랙션 둘 이상에 페치 조인을 사용하면 안된다.
     * => 데이터가 부정합하게 조회될 수 있다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        for (Order order : orders) {
            // JPA에서는 PK(id)가 똑같으면 완전히 동일한 객체이다.
            System.out.println("order ref = " + order + ", order id = " + order.getId());
        }

        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * V3.1 : 컬랜션 엔티티 + 페이징 한계 돌파
     * <p>
     * [컬랙션 엔티티 + 페이징 조회 방법]
     * 1) 먼저 ToOne(OneToOne, ManyToOne) 관계를 모두 페치조인 한다.
     * => ToOne 관계는 row수를 증가시키지 않으므로, 페이징 쿼리에 영향을 주지 않는다.
     * 2) 컬렉션은 지연 로딩으로 조회한다.
     * 3) 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size, @BatchSize를 적용한다.
     * => hibernate.default_batch_fetch_size: 글로벌 설정
     * => @BatchSize: 개별 최적화
     * => 이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다.
     * <p>
     * (적당한 default_batch_fetch_size 고르기)
     * => Minimum은 없지만 Maximum은 1000이다. (데이터베이스에 따라 IN 절 파라미터를 1000으로 제한하기도 해서)
     * => default_batch_fetch_size의 크기는 100~1000 사이를 선택하는 것을 권장한다.
     * <p>
     * (CPU와 같은 리소스 사용 측면)
     * => 1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션(WAS)이든 순간 부하(CPU같은 리소스 사용 측면)를 어디까지 견딜 수 있는지로 결정하면 된다.
     * <p>
     * (WAS 입장에서 메모리 사용 측면)
     * => 하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량은 같다.
     * => 특정 시점에 JVM에서 사용하는 메모리는 해당 옵션과 무관하게 대부분 다 찬다.
     * => 즉, 100이든 1000이든 outOfMemory가 발생할 확률은 거의 같다.
     * <p>
     * <p>
     * [장점]
     * 1. DB에서 데이터 중복 없이 최적화하여 가져온다.
     * => 네트워크 용량 최소화
     * => 조인보다 DB 데이터 전송량이 최적화 된다.
     * => 이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없다.
     * <p>
     * 2. 쿼리 호출 수 가 1+N => 1+1로 최적화 된다.
     * <p>
     * 3. 컬랙션 페치 조인은 페이징이 불가능하지만 이 방법은 페이징이 가능하다.
     * <p>
     * [문제점]
     * 1. 컬랙션 엔티티까지 fetch join으로 한방 쿼리로 가져오는 것보다, 쿼리가 많이 나간다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        // Member와 Delivery만 한번에 fetch join해온다. => xtoOne 연관 관계이므로 페치조인 가능!
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * V4 : JPA에서 DTO 직접 조회
     * <p>
     * 1) Query : 루트 1번, 컬렉션 N번 실행
     * 2) ToOne(N:1, 1:1) 관계들은 먼저 조회하고, ToMany(1:N) 관계는 각각 별도로 처리한다.
     * => 이런 방식을 선택한 이유는 다음과 같다.
     * => ToOne 관계는 조인해도 데이터 row 수가 증가하지 않는다.
     * => ToMany 관계는 조인하면 row 수가 증가한다.
     * 3) row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화하기 쉬우므로 한번에 조회하고,
     * ToMany 관계는 최적화하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회한다.
     * <p>
     * [장점]
     * 1. select할 때 원하는 데이터만 DTO에 맞춰서 가져오므로, 네트워크 용량이 줄어든다.
     * <p>
     * [문제점]
     * 1. N+1 문제가 발생한다.
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * V5 : JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
     * <p>
     * 1) Query : 루트 1번, 컬렉션 1번
     * 2) ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계인 OrderItem을 한꺼번에 조회
     * 3) MAP을 사용해서 매칭 성능 향상 => O(1)
     *
     * [장점]
     * 1. select할 때 원하는 데이터만 DTO에 맞춰서 가져오므로, 네트워크 용량이 줄어든다.
     * 2. N+1 문제가 발생하지 않는다. 쿼리는 총 1+1 만큼 실행된다.
     *
     * [문제점]
     * 1. 코드가 복잡하다.
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }


    /**
     * V6 : JPA에서 DTO 직접 조회 - 플랫 데이터 최적화
     * <p>
     * [장점]
     * 1. select할 때 원하는 데이터만 DTO에 맞춰서 가져오므로, 네트워크 용량이 줄어든다.
     * 2. 쿼리가 한번만 실행된다. => 모든 엔티티를 join해서 한방 쿼리 실행
     *
     * [문제점]
     * 1. 데이터를 중복해서 가져온다.
     * => 네트워크 용량 증가
     * => 쿼리는 한번이지만 조인으로 인해, DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5보다 느릴 수도 있다.
     *
     * 2. Order를 기준으로 페이징할 수 없다.

     * 3. API 스펙에 맞추기 위해서 코드가 복잡해진다.
     * => 애플리케이션에서 추가 작업이 크다.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(
                        groupingBy(
                                o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                                mapping(
                                        o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()),
                                        toList())))
                .entrySet()
                .stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }


    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();     // LAZY LOADING => 프록시 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();     // LAZY LOADING => 프록시 초기화
            orderItems = order.getOrderItems().stream()
                    .map(oi -> new OrderItemDto(oi))
                    .collect(Collectors.toList());
/*            order.getOrderItems().stream()
                    .forEach(oi-> oi.getItem().getName());  // LAZY LOADING => 프록시 초기화

            orderItems = order.getOrderItems();*/
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;    // 상품명
        private int orderPrice;     // 주문 가격
        private int count;          // 주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
