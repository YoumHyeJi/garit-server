package com.garit.study;

import com.garit.study.domain.Member;
import com.garit.study.repository.MemberRepositoryOld;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepositoryOld memberRepository;

    /**
     * tdd 단축키 생성
     * Settings -> Live Template
     */
    @Test
    /**
     * 모든 EntityManager를 통한 데이터 변경은 Transaction 안에서 이뤄줘야한다.
     * @Transactional 어노테이션이 테스트케이스에 붙어있으면, 테스트가 끝난 후 데이터가 모두 rollback 된다.
     */
    @Transactional
    /**
     * false로 설정하면 rollback 되지 않는다.
     */
    @Rollback(false)
    public void testMember() throws Exception{
        // given
        Member member = new Member();
        member.setName("memberA");

        // when
        /**
         * insert 쿼리 나감
         */
        Long saveId = memberRepository.save(member);
        /**
         * 이미 영속성 컨텍스트의 1차 캐시에 존재하는 앤티티를 조회하기 때문에, select 쿼리가 나가지 않는다.
         */
        Member findMember = memberRepository.findOne(saveId);

        // then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getName()).isEqualTo(member.getName());

        /**
         * 같은 트랜잭션 내에서 동일한 아이디로 저장하고 조회하는 앤티티는 같다.
         * 즉 같은 영속성 컨텍스트의 1차 캐시 내에서 아이디가 같으면, 같은 앤티티로 식별한다.
         */
        Assertions.assertThat(findMember).isEqualTo(member);
        System.out.println("findMember == member : " + (findMember == member));
    }

}