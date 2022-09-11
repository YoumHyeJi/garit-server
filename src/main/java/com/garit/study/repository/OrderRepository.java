package com.garit.study.repository;

import com.garit.study.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Repository는 순수한 엔티티, 혹은 엔티티와 연관된 객체 그래프를 탐색할 때 사용함.
 */
@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m";
        return em.createQuery(jpql, Order.class)
                .setMaxResults(1000)    // 최대 1000건 조회
                .getResultList();
    }

/*    public List<Order> findAll(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m " +
                "where o.status = :status " +
                "and m.name like :name";
        return em.createQuery(jpql, Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setFirstResult(100)    // 100번째부터 조회
                .setMaxResults(1000)    // 최대 1000건 조회
                .getResultList();
    }*/

    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * [fetch join]
     * 한방쿼리로 Order, Member, Delivery를 join해서,
     * select 절에서 관련 정보를 모두 가져온다.
     * => LAZY를 무시하고 진짜 값을 다 채워서 가져온다. (프록시 객체 X)
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class).getResultList();
    }


    /**
     * 페이징 파라미터 (offset, limit)을 받는다.
     * => xToOne 관계를 페치 조인한 경우에는, 페이징 처리가 정상적으로 된다.
     */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }


    public List<Order> findAllWithItem() {
        /**
         * 1:N 연관관계에서 페치 조인을 하면, 데이터가 N만큼 증가된다.
         * distinct 키워드를 넣어주면, 중복을 제거해줄 수 있다.
         *
         * [JPA distinct 키워드의 두가지 기능]
         * 1) DB SQL에 distinct 키워드를 붙여준다.
         * 2) 쿼리 결과로 받은 엔티티 리스트 중에 중복이 존재하면, 이를 제거해준다.
         * => JPA의 distinct 키워드는 Order가 같은 PK(id) 값을 가질 때, 중복으로 간주하고 한 엔티티를 제거해준다.
         * => cf) DB의 distinct 키워드는 두 행이 완전히 똑같아야, 중복으로 간주하고 한 행을 제거해준다.
         *
         * !!그러나 페이징이 안되는 단점이 있다!!
         */
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .setFirstResult(1)
                .setMaxResults(100)
                .getResultList();
    }


}
