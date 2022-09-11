package com.garit.study.service;

import com.garit.study.domain.Member;
import com.garit.study.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

/**
 * JPA의 모든 데이터 변경이나 로직은 트랜잭션 안에서 이뤄줘야 한다.
 * public method들은 기본적으로 트랜잭션이 걸린다.
 */
@Transactional(readOnly = true)

/**
 * final이 붙은 필드만 가지고 생성자를 만들어준다.
 */
@RequiredArgsConstructor
public class MemberService {

    /**
     * 1. field injection (필드 인잭션)
     * 스프링이 스프링 빈에 등록되어 있는 MemberRepository를 injection해준다.
     *
     * (단점) 그러나 필드 인잭션의 경우, private로 설정된 필드이기 때문에 변경 불가능하다는 단점이 있다.
     * => 때떄로 테스트 코드를 작성할 때 필드 값을 변경해야하는 경우가 있다.
     *
     *     @Autowired
     *     private MemberRepository memberRepository;
     */

    /**
     * 2. setter injection (세터 인잭션)
     * 스프링이 스프링 빈에 등록되어 있는 MemberRepository를 injection해준다.
     *
     * (장점) 메서드를 통해 객체를 주입하기 때문에, 테스트 코드를 작성할 때 Mock 객체를 직접 주입해줄 수 있다.
     *
     * (단점) Run time (실제 애플리케이션이 돌아가는 시점)에 누군가가 객체를 바꿀 수 있다.
     * => 애플리케이션 로딩 시점에 주입이 완료된 후 변경할 필요가 없다.
     *
     *     @Autowired
     *     public void setMemberRepository(MemberRepository memberRepository) {
     *         this.memberRepository = memberRepository;
     *     }
     */

    /**
     * 3. constructor injection (생성자 인잭션)
     * 스프링이 스프링 빈에 등록되어 있는 MemberRepository를 injection해준다.
     *
     * (장점) 한번 생성할 때 객체 주입이 끝나기 때문에, 중간에 변경될 가능성이 없다.
     * (장점) 테스트 케이스를 작성할 때, Mock 객체를 생성자에 주입할 수 있다.
     *
     *     @Autowired
     *     public MemberService(MemberRepository memberRepository) {
     *         this.memberRepository = memberRepository;
     *     }
     */

    /**
     * final을 붙이면 컴파일 시점에 객체를 주입 받는지 확인할 수 있다.
     */
    private final MemberRepository memberRepository;


    /**
     * 회원 가입
     *
     * 따로 @Transactional 어노테이션을 붙이면, 해당 어노테이션의 우선순위가 더 높게 걸린다.
     * 기본적으로 readOnly=false이기 때문에 쓰기전용 트랜잭션이 된다.
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);    // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * WAS가 동시에 여러 개가 뜨기 때문에, 동시에 validateDuplicateMember() 로직을 타게 되면 validation을 통과할 수 있다.
     * 따라서 멀티스레드 환경을 고려해서, DB의 name 필드에 unique 제약 조건을 추가한다.
     */
    private void validateDuplicateMember(Member member) {
        // EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    // 특정 회원 조회
    public Member findOne(Long memberId){
        return memberRepository.findById(memberId).get();
    }


    /**
     * 변경감지를 통한 name 수정
     * 트랜잭션 내에서 조회하면, 영속성 컨텍스트 내에서 엔티티를 가져온다. => 변경감지 사용 가능!
     *
     * command(수정)과 query(조회)를 철저하게 분리하자!
     */
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findById(id).get();
        member.setName(name);
    }
}
