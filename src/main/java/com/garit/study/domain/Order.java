package com.garit.study.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // new로 생성하지 못하도록한다.
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 연관관계의 주인
    // 해당 필드의 값이 변경되면, FK값(member_id)이 변경됨
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * 기본적으로 엔티티를 영속성 컨텍스트에 저장하고 싶으면, 각각 모든 엔티티에 대해서 persist 해줘야 한다.
     * cascade는 persist를 전파한다.
     * order만 persist해도, order의 orderItems에 저장된 엔티티들도 persist된다.
     * delete의 경우도 마찬가지다.
     * 트랜잭션이 commit되는 시점에 flush가 되면서, insert or delete 쿼리가 함께 날아간다.
     *
     * 다른 곳에서 orderItem을 참조하지 않기 때문에 (private owner) cascade 속성을 쓸 수 있다.
     * 즉, lifecycle이 일치하고, private owner일 때 cascade속성을 쓸 수 있다.
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


    /**
     * 생성 메서드
     * => 주문 생성에 대한 복잡한 비즈니스 로직을 완결!
     */
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        Order order = new Order();

        // 연관관계를 걸면서 세팅이된다.
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    /**
     * 비즈니스 로직
     */


    /**
     *  엔티티안에서 데이터가 변경될 경우, dirty checking(변경 내역 감지)이 일어난다.
     *  변경 내역을 찾아서 DB에 update 쿼리가 자동으로 날아간다.
     *  1. order의 status에 대한 update 쿼리 생성
     *  2. item의 stockQuantity에 대한 update 쿼리 생성
     */
    public void cancel(){       // 주문 취소

        /**
         * 이미 배송 완료된 상품은 취소가 불가능
         * => 비즈니스 로직에 대한 체크 로직이 엔티티안에 있음!
         */
        if (delivery.getStatus() == DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        // Order의 status를 CANCEL로 변경
        this.setStatus(OrderStatus.CANCEL);

        /**
         *  회원이 상품을 두개 주문 하면, OrderItem은 두개가 생긴다.
         *  따라서 각각의 OrderItem에 대해 모두 cancel을 해줘야한다.
         */
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    /**
     * 조회 로직
     */
    // 전체 주문 가격 조회회
    public int getTotalPrice(){
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;

/*
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
*/
    }

}
