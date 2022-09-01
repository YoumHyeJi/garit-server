package com.garit.study.service;

import com.garit.study.domain.item.Book;
import com.garit.study.domain.item.Item;
import com.garit.study.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item){
        itemRepository.save(item);
    }

    /**
     * [dirty checking]
     * 1) 스프링의 @Transactional 어노테이션에 의해 트랜잭션이 커밋된다.
     * 2) 트랜잭션이 커밋되는 시점에 JPA가 flush를 날린다.
     * 3) flush를 날리게 되면, 영속성 컨텍스트에 저장된 엔티티 중에 변경된 내용을 모두 찾는다.
     * 4) 찾은 변경 내용이 있으면, DB에 update 쿼리를 날린다.
     *
     *
     * [merge (=병합)]
     * => 병합은 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능이다.
     * => 그러나 준영속 엔티티가 영속 엔티티로 바뀌는 것은 아니고, 영속 엔티티를 반환할 뿐이다!
     * 1) merge()를 실행한다.
     * 2-1) 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
     * 2-2) 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
     * 3) 조회한 영속 엔티티(mergeMember)에 member 엔티티의 값을 채워 넣는다.
     * (member 엔티티의 모든 값을 mergeMember에 밀어 넣는다. 이때 mergeMember의 “회원1”이라는 이름이 “회원명변경”으로 바뀐다.)
     * 4) 영속 상태인 mergeMember를 반환한다.
     *
     *
     * [dirty checking과 merge 중 무엇을 사용해야 할까?]
     * => 변경 감지 기능을 사용하면, 원하는 속성만 선택해서 변경할 수 있다.
     * => 그러나 병합을 사용하면, 모든 속성이 변경된다. (병합시 값이 없으면 null로 업데이터할 위험도 있다.)
     * => 따라서 실무에서는 변경 감지를 사용해야 한다.
     */
    @Transactional
    public Item updateItem(Long itemId, String name, int price, int stockQuantity){
        // itemId를 기반으로 영속 엔티티를 조회해온다.
        Item findItem = itemRepository.findOne(itemId);

        // 변경 지점이 엔티티 레벨에 있는 것이 좋다. => 추적이 쉽도록!
        // setter()를 사용하기 보다는 의미있는 비즈니스 메서드를 만들자. e x) addStock()
        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
        /**
         * itemRepository에서 save, merge, persist 등 아무것도 호출할 필요가 없다!
         */
        return findItem;
    }

    public List<Item> findItems(){
        return itemRepository.findALl();
    }

    public Item findOne(Long id){
        return itemRepository.findOne(id);
    }
}
