package com.garit.study.api;

import com.garit.study.domain.Address;
import com.garit.study.domain.Order;
import com.garit.study.domain.OrderStatus;
import com.garit.study.repository.OrderRepository;
import com.garit.study.repository.OrderSearch;
import com.garit.study.repository.order.simplequery.OrderSimpleQueryDto;
import com.garit.study.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne (ManyToOne, OneToOne)
 *
 * Order
 * Order -> Member (N:1)
 * Order -> Delivery (1:1)
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * [문제점]
     * 1. Order도 Member를 가지고 있고, Member도 Order를 가지고 있다. (양방향 연관관계)
     * 따라서 무한 루프에 빠지면서 객체를 탐색하게 된다.
     *
     * => JsonIgnore로 해결
     * => Order와 양방향 연관관계를 가지고 있는 엔티티에서, Order 필드에 @JsonIgnore 어노테이션을 붙여준다.
     * => ex) Member, OrderItem, Delivery
     *
     *
     * 2. 지연로딩 (fetch = Lazy)으로 설정된 객체는 DB에서 가져오지 않는다.
     * 따라서 지연로딩 설정된 Member 객체는 DB에서 가져오지 않고, Order 객체만 가져오는 것이다.
     *
     * 그러나 Member에 null을 넣어둘 수 없기 때문에, 프록시 Member 객체를 넣어둔다.
     * private Member member = new ByteBuddyInterceptor();
     * 프록시 객체를 가짜로 넣어놓고, Member 객체 값을 사용할 때 그때 DB에서 Member 객체를 진짜 가져온다. => 프록시 초기화!
     *
     * 결국 Jackson 라이브러리가 프록시 객체를 이해하지 못하고 Type definition error가 발생한다.
     *
     * => 지연로딩인 경우에는 Jackson 라이브러리가 해당 객체를 가져오지 않도록, Hibernate5Module을 설치해야 한다.
     *
     *
     * 3. 엔티티를 외부에 그대로 노출하면, 엔티티 스펙이 변경된 경우 API 스펙도 변경되는 문제가 있다.
     * => 가급적 필요한 데이터만 API 스페에 노출해야한다.
     * => 따라서 Hibernate5Module을 사용하기보다는 엔티티를 DTO로 변환해서 API 응답으로 반환해야 한다.
     *
     * 4. 성능상 좋지 않다.
     * API 스펙상 OrderItem 리스트는 필요 없지만, 옵션(FORCE_LAZY_LOADING) 때문에 강제로 DB에서 가져와야 한다.
     * 따라서 불필요한 쿼리가 DB로 나가기 때문에 성능상 좋지 않다.
     *
     * => 그렇다고 LAZY를 EAGER로 바꾸면 안된다!!
     * => JPQL을 사용할 경우, EAGER로 바꾸면 성능 최적화가 안된다.
     * => JPQL을 그대로 SQL로 번역되기 때문에, 우선 Order만 가져온 후 EAGER 설정으로 된 연관 객체들을 강제로 단건으로 조회한다.
     * => n+1 문제 발생
     * => 따라서 항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우엔 페치 조인(fetch join)을 사용하라!
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            /**
             * 강제로 LAZY LOADING하기
             *
             * => order.getMember()를 통해 가져온 Member객체는 프록시 객체이다.
             * => order.getMember().getName()을 통해 실제 name을 DB에서 가져와야한다.
             * => LAZY 강제 초기화!!
             * => Hibernate5Module이 이미 초기화된 객체는 결과로 출력한다.
             */
            order.getMember().getName();
            order.getDelivery().getAddress();
        }
        return all;
    }

    /**
     * [장점]
     * 1. DTO를 사용하여, API 스펙에 맞게 최적화했다.
     *
     * [문제점]
     * 1. LAZY LOADING으로 인해, DB에 쿼리가 너무 많이 날아가는 문제점이 있다.
     * => N+1 문제
     * => 첫번째(1) 쿼리의 결과(N) 만큼, 쿼리가 N번 추가로 수행되는 문제이다.
     * => 지연 로딩은 영속성 컨텍스트를 먼저 조회하므로, 이미 조회된 경우 쿼리를 생략한다.
     * => fetch 전략을 EAGER로 바꿔도, JPQL을 사용하면 최적화가 안된다.
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){

        // Order를 조회한 결과 : 2개 (N)
        // N + 1    =>      1 + 회원 N + 배송 N     =>      1 + 2 + 2       =>      쿼리 5번
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }


    /**
     * [장점]
     * 1. 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
     * => 페치 조인으로 oder -> member, order -> delivery 는 이미 조회된 상태이므로 지연로딩 X
     *
     * 2. 외부 (Order)를 건들이지 않으면서,
     * 내부의 원하는 것(Member, Delivery)만 fetch 조인으로 성능 튜닝을 하면서 조회할 수 있다.
     *
     * 3. 재사용성이 높다.
     * => 많은 API에서 사용가능하다.
     *
     * [문제점 = Trade Off]
     * 1. 데이터를 DB에서 많이 가져오므로, 네트워크를 많이 사용한다.
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    /**
     * [장점]
     * 1. 결과를 DTO로 반환받기 때문에, select 절에서 원하는 정보만 조회가 가능하다.
     * => 데이터를 DB에서 적게 가져오므로, 네트워크를 덜 사용한다.
     * => 즉, Select 절에서 원하는 데이터를 직접 선택하므로, DB -> 애플리케이션 네트웍 용량 최적화 (생각보다 미비)
     *
     * [문제점 = Trade Off]
     * 1. Repository 재사용성이 떨어진다.
     * => OrderSimpleQueryDto를 사용할 때만 사용할 수 있다.
     * => API 스펙에 맞춘 코드가 Repository에 들어가는 단점
     * => Repository는 엔티티에 대한 객체 그래프를 탐색하는데 사용이 되어야 한다.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }


    /**
     * [쿼리 방식 선택 권장 순서]
     * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
     * 2. 필요하면 페치 조인으로 성능을 최적화 한다.  대부분의 성능 이슈가 해결된다.
     * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
     * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
     */


    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            /**
             * LAZY 초기화
             * => 영속성 컨텍스트에서 memberId를 가지고 엔티티를 찾아본다.
             * => 영속성 컨텍스트에 해당 엔티티가 존재하지 않으면, DB에 쿼리를 날려서 조회해온다.
             */
            name = order.getMember().getName();     // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();     // LAZY 초기화
        }
    }


}
