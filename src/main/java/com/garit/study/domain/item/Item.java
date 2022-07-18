package com.garit.study.domain.item;

import com.garit.study.domain.Category;
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
}
