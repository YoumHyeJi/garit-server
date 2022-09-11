package com.garit.study.domain.item;

import com.garit.study.domain.Category;
import com.garit.study.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity

// 부모 테이블에 상속관계 전략을 명시해줘야 한다.
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)

// single table이기 때문에, discriminatorColumn으로 각 엔티티를 구분해줘야 한다.
@DiscriminatorColumn(name = "dtype")

@Getter
@Setter

// abstract를 통해 추상 클래스로 만든다. => 구현체가 따로 있기 때문에!
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

    // 다대다 관계
    // mappedBy로 items를 잡아주면 됨됨
    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();


    /**
     * 비즈니스 로직
     * => 상품 재고 수량(stockQuantity)이 Item 엔티티 안에 있기 때문에,
     * 상품 재고 수량 관리 비즈니스 메서드는 Item 엔티티 안에 만드는 것이 좋다.
     *
     * 도메인 주도 설계
     * => 엔티티 자체에서 해결할 수 있는 문제는 엔티티 안에 비즈니스 메서드를 만들어서 해결한다.
     * => 데이터를 가지고 있는 쪽에서 비즈니스 메서드를 만들어야 응집력을 높일 수 있다.
     */

    /**
     * stock 증가
     */
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0){
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
