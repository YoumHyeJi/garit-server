package com.garit.study.domain;

import com.garit.study.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    // 다대다 관계 => 조인 테이블이 필요
    // 조인 테이블에 새로운 필드를 추가하는게 불가능하기 때문에, 실무 사용 X
    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    /**
     * 같은 엔티티에 대한 계층구조는
     * 같은 엔티티에 대해서 self로 양방향 연관관계를 건 것이다.
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();
}
