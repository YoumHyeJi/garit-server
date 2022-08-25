package com.garit.study.repository;

import com.garit.study.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

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
}
