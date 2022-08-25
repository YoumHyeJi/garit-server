package com.garit.study.domain;

import com.garit.study.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

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
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * 컬렉션은 필드에서 초기화하자.
     *
     * 1. new ArrayList<>() 로 초기화하면, null point exception이 발생하지 않는다.
     *
     * 2. 하이버네이트는 엔티티를 영속화(em.persist)할 때, 컬랙션을 감싸서 하이버네이트가 제공하는 내장 컬래션으로 변경한다.
     * 만약 getChild()처럼 임의의 메서드에서 컬랙션을 잘못 생성하면, 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다.
     * 따라서 필드 레벨에서 생성하는 것이 안전하고 코드도 간결하다.
     */
    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    /**
     * 양방향 연관관계 편의 메서드
     */
    public void addChildCategory(Category child){
        this.child.add(child);
        child.setParent(this);
    }

    /**
     * 해당 연관관계 편의 메서드는 필요없나?
     */
/*    public void addItem(Item item){
        this.items.add(item);
        item.getCategories().add(this);
    }*/
}
