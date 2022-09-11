package com.garit.study.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 별도로 쿼리용 Repository를 만든다.
 * 화면과 연관된 (API 스펙과 연관된) 복잡한 쿼리를 작성한다.
 * => 유지 보수성이 증가한다.
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    /**
     * Repository에서 Controller로 의존관계가 생기면 안된다.
     * 의존관계는 Controller => Service => Repository 한방향으로 흘러야한다.
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        /**
         * new 명령어를 사용해서 JPQL을 결과를 DTO로 즉시 변환
         * => new 오퍼레이션의 생성자에서 엔티티를 바로 넘길 수 없다.
         * => 엔티티를 넘기면 식별자로 변환되어 넘어가기 때문이다.
         * => 오직 값타입 (Embeddable 타입)과 기본 타입만 넘길 수 있다.
         *
         * => 논리적 계층이 깨지고, Repository가 화면에 의존한다.
         * => 화면 (또는 API 스펙)이 변경되면, 해당 코드도 변경되어야 한다.
         */
        return em.createQuery(
                        "select new com.garit.study.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                                " from Order o" +
                                " join o.member m"+
                                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();

    }
}
