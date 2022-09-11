package com.garit.study.repository;

import com.garit.study.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

// component scan의 대상이 돼서 자동으로 스프링 빈에 등록된다.
@Repository

// Spring Data JPA 라이브러리가 없으면, EntityManger를 @Autowired 어노테이션으로 주입 받을 수 없다. => @PersistenceContext 어노테이션 사용!
@RequiredArgsConstructor
public class MemberRepositoryOld {

    /**
     * 스프링 부트를 사용하기 때문에 Spring Container 위에서 모든게 동작된다.
     * 스프링 부트가 생성한 EntityManager를 주입해준다.
     *     @PersistenceContext
     *     private EntityManager em;
     *
     *     @PersistenceUnit
     *     private EntityManagerFactory emf;
     */

    private final EntityManager em;

    /**
     * Command와 Query를 분리해라
     * 저장은 side effect를 발생시킬 수 있는 command 성을 가졌다.
     * 따라서 return값으로 Member를 주지 않고, Id를 준다.
     */
    public Long save(Member member){
        /**
         * em.persist()하는 순간, JPA가 영속성 컨텍스트에 key-value 형태로 Member 엔티티를 저장해준다.
         * key에는 Member 엔티티의 id값(테이블의 PK 값)이, value에는 Memeber 엔티티가 저장된다.
         *
         * em.persist()한다고 바로 insert 쿼리가 날아가는 것은 아니다.
         *
         * 나중에 데이터베이스 트랜잭션이 커밋되고 flush 되는 순간에, 영속성 컨텍스트에 존재하던 Member 엔티티가 DB에 반영된다. (insert 쿼리 날아감)
         */
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
