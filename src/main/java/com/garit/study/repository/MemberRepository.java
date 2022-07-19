package com.garit.study.repository;

import com.garit.study.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

// component scan의 대상이 돼서 자동으로 스프링 빈에 등록된다.
@Repository
public class MemberRepository {

    /**
     * 스프링 부트를 사용하기 때문에 Spring Container 위에서 모든게 동작된다.
     * 스프링 부트가 생성한 EntityManager를 주입해준다.
     */
    @PersistenceContext
    private EntityManager em;

/*
    @PersistenceUnit
    private EntityManagerFactory emf;*/

    /**
     * Command와 Query를 분리해라
     * 저장은 side effect를 발생시킬 수 있는 command 성을 가졌다.
     * 따라서 return값으로 Member를 주지 않고, Id를 준다.
     */
    public Long save(Member member){
        // JPA가 영속성 컨텍스트에 Member 엔티티를 저장해준다.
        // 나중에 트랜잭션이 커밋되는 순간에 DB에 반영된다. (insert 쿼리 날아감)
        em.persist(member);
        return member.getId();
    }

    public Member findOne(Long id){
        // JPA가 Member 엔티티를 검색해준다.
        // 파라미터 : 조회타입, PK
        return em.find(Member.class, id);
    }

    /**
     * JPQL은 SQL과 기능적으로 거의 유사하다.
     * 왜냐하면 JPQL이 결국 SQL로 변환되기 때문이다.
     *
     * SQL은 테이블을 대상으로 쿼리를 날리지만, JPQL은 엔티티를 대상으로 쿼리를 날리는게 차이점이다.
     */
    public List<Member> findAll(){
        // 파라미터 : JPQL, 반환타입
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name){
        // 파라미터 바인딩
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
