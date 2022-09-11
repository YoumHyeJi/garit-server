package com.garit.study.repository;

import com.garit.study.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    /**
     * JPA를 이용해서 persist하기 전까지 Item의 id값은 없다.
     *
     * 1. id 값이 없다는 것은 새로 생성한 객체라는 의미이다.
     * 따라서 em.persist()를 이용해서 신규로 저장해줘야한다.
     *
     * 2. id 값이 이미 DB에 존재하는 데이터를 가져온 것이다.
     * 따라서 em.merge()를 이용해서 업데이트해줘야 한다.
     */
    public void save(Item item){
        if(item.getId() == null){
            em.persist(item);
        }
        else{
            em.merge(item);
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findALl(){
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
