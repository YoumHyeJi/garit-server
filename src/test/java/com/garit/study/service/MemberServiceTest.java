package com.garit.study.service;

import com.garit.study.domain.Member;
import com.garit.study.repository.MemberRepository;
import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@SpringBootTest

/**
 * @Transactional 어노테이션이 테스트 클래스에 붙어있으면, 기본적으로 모두 Rollback 시킴.
 * Service나 Repository 클래스에서는 Rollback시키지 않음.
 */
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
//    @Rollback(value = false)    // 트랜잭션이 롤백되지 않아서 데이터가 DB에 들어간다.
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("kim");

        //when
        Long saveId = memberService.join(member);

        //then

        /**
         * flush()를 통해 영속성 컨텐스트에 있는 등록이나 변경 내용을 DB에 반영함
         */
        em.flush();
        Assertions.assertThat(member).isEqualTo(memberRepository.findOne(saveId));

        /**
         * 같은 트랜잭션 내에서 pk값이 동일하면, 영속성 컨텍스트에서 하나의 엔티티로 관리된다.
         *
         * 가장 좋은 테스트 방법은 WAS를 띄울 때, 메모리 DB를 같이 띄워서 테스트하는 것이다.
         * 애플리케이션이 종료되면 메모리 DB도 내려간다.
         */


    }

    @Test
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);

        Assertions.assertThatCode(()->{
            memberService.join(member2);    // 예외가 발생해야 한다!!
        }).isInstanceOf(IllegalStateException.class);
    }
}