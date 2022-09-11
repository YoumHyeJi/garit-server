package com.garit.study.api;

import com.garit.study.domain.Member;
import com.garit.study.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

// @Controller + @ResponseBody = @RestController
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * [문제점]
     * 1) 엔티티를 직접 노출하면, 엔티티의 모든 정보가 외부에 노출된다.
     * <p>
     * => 엔티티의 필드에 @JsonIgnore 같은 어노테이션을 붙여 외부로 노출되지 않게 할 수 있지만,
     * => 이는 엔티티에 Presentation 계층의 로직이 들어간 것이다.
     * => 이로 인해 엔티티로 의존관계가 들어와야 하지만, 엔티티에서 의존관계가 나가버리게 된다.
     * => 양방향 의존관계가 걸리면, 어플리케이션을 수정하기 어려워진다.
     * <p>
     * 2) 엔티티 스펙이 바뀌면, API 스펙도 바뀐다.
     * 3) 엔티티 리스트를 반환하면, API 스펙을 확장할 수 없다. ex) count 추가 불가
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * API를 만들 때는 절대 파라미터로 Entity를 노출 시키면 안된다.
     * 중간에 API 스펙에 맞는 DTO를 만들어서 사용해야 한다.
     */
    @GetMapping("/api/v2/members")
    public Result<MemberDto> membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    /**
     * [문제점]
     * 1) Presentation Layer를 위한 검증 로직이 Entity에 들어가 있다.
     * 2) Entity의 스펙이 바뀌면, API의 스펙도 바뀐다.
     * 3) API 스펙을 확인하지 않는 이상, Member의 어느 값이 파라미터로 들어오는지 모른다.
     * <p>
     * => Entity와 API 스펙이 1대1로 매핑되어 있으면 안된다.
     * => API 스펙을 위한 DTO를 만들어야 한다.
     * => API 요청 스펙에 맞춰서 별도의 DTO를 파라미터로 받아야 한다.
     * => Entity를 외부에 노출해서도 안된다.
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * [장점]
     * 1) Entity 스펙이 바뀌어도, API 스펙이 바뀌지 않는다.
     * 2) API 스펙에 맞게, validation을 따로 해줄 수 있다.
     * 3) 파라미터로 어떤 값이 들어오는지 바로 알 수 있다.
     * <p>
     * => API는 요청이 들어오고 나갈때 DTO를 파라미터로 사용해야 한다.
     * => 절대 Entity 그 자체를 외부로 노출 시켜서 파라미터로 사용하면 안된다!
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberRequest {

        @NotEmpty
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }
}
