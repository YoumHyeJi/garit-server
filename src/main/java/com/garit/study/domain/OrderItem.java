package com.garit.study.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garit.study.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;


import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@BatchSize(size = 100)      // ToOne 연관관계인 경우, 클래스에 @BatchSize 어노테이션 적용!
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)      // new로 생성하지 못하도록한다.
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 당시 가격

    private int count;      // 주문 당시 수량


    /**
     * 생성 메서드
     * => new가 아닌 해당 메서드로 OrderItem을 생성해야 한다.
     */
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();

        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        // 아이템 재고 감소
        item.removeStock(count);

        return orderItem;
    }

    /**
     * 비즈니스 로직
     */
    public void cancel() {
        // 재고 수량 원복
        getItem().addStock(count);
    }

    /**
     * 조회 로직
     */

    // 주문 상품 전체 가격 조회
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
