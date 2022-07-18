package com.garit.study.repository;

import com.garit.study.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

// component scan의 대상이 돼서 자동으로 스프링 빈에 등록된다.
@Repository
public class MemberRepository {

    /**
     * 스프링 부트를 사용하기 때문에 Spring Container 위엥서 모든게 동작된다.
     * 스프링 부트가 EntityManager를 주입해준다.
     */
    @PersistenceContext
    private EntityManager em;

    /**
     * Command와 Query를 분리해라
     * 저장은 side effect를 발생시킬 수 있는 command 성을 가졌다.
     * 따라서 return값으로 Member를 주지 않고, Id를 준다.
     */
    public Long save(Member member){
        em.persist(member);
        return member.getId();
    }

    public Member find(Long id){
        return em.find(Member.class, id);
    }
}
