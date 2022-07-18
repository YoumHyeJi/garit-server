package com.garit.study.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 연관관계의 주의
    // 해당 필드의 값이 변경되면, FK값(member_id)이 변경됨
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * 기본적으로 엔티티를 영속성 컨텍스트에 저장하고 싶으면, 각각 모든 엔티티에 대해서 persist 해줘야 한다.
     * cascade는 persist를 전파한다.
     * order만 persist해도, order의 orderItems에 저장된 엔티티들도 persist된다.
     * delete의 경우도 마찬가지다.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * 모든 연관관계는 "지연 로딩"으로 설정!
     *
     * 즉시 로딩은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다.
     * 특히 즉시 로딩은 JPQL을 실행할 때, N+1 문제가 자주 발생한다.
     * => JPQL (select o from Order o)를 실행했을 때 결과로 N개의 엔티티가 조회되면, 각 엔티티와 관련된 Delivery를 조회하기 위해 N번의 쿼리가 또 실행됨!
     *
     * 따라서 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join이나 엔티티 그래프 기능을 사용한다.
     */
    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;    // 주문 시간

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;         // 주문 상태 [ORDER, CANCEL]

    /**
     * 양방향 연관관계 편의 메서드
     * 핵심적으로 control하는 쪽에 연관관계 편의 메서드가 있는 것이 좋다.
     */
    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
        /*
            public static void main(String[] args) {
            Member member = new Member();
            Order order = new Order();
            member.getOrders().add(order);
            order.setMember(member);
        }*/
    }

    public void addOrderItem(OrderItem orderItem){
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }


}
