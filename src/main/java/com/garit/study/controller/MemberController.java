package com.garit.study.controller;

import com.garit.study.domain.Address;
import com.garit.study.domain.Member;
import com.garit.study.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * createMemberForm 화면을 여는 url.
     *
     * Get /members/new로 요청이 들어오면,
     * MemberController를 통해
     * createMemberForm.html이 렌더링 된다.
     *
     * MemberController에서 model의 attribute로 memberForm 객체를 넘겼다.
     * 따라서 화면에서 해당 객체(memberForm)에 접근할 수 있게 된다.
     */
    @GetMapping("/members/new")
    public String createForm(Model model){
        // Controller에서 View로 넘어갈 때, Model에 데이터를 심어서 넘긴다.
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * memberForm 데이터를 실제로 등록하는 url.
     *
     * @Valid 어노테이션을 붙임으로써, 자동으로 validation을 해준다.
     * 만약 validation 과정에서 error가 발생하면, BindingResult에 해당 내용이 담긴다.
     * 스프링과 thymeleaf는 연동이 매우 잘되어있어서, BindingResult에 담긴 내용을 화면에 넘겨준다.
     *
     * 회원가입 후에는 재로딩하지 않고, 리다이렉트로 첫번째 페이지로 이동
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result){

        if(result.hasErrors()){
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);

        return "redirect:/";
    }

    /**
     * Model 객체를 통해 화면에 데이터를 전달한다.
     *
     * 원래는 Entity를 그대로 화면에 전달하지 않고,
     * Form 객체나 DTO를 사용해서 전달해야한다.
     * => Entity는 핵심 비즈니스 메서드와만 dependency를 가져야하기 때문이다.
     *
     * 특히 API를 개발할때는 절대로 엔티티를 반환해서는 안된다.
     * => 엔티티가 변경됐을 때 API 스펙이 변하고,
     * => 중요한 정보(password 등)가 노출될 수 있기 때문이다.
     *
     * 해당 예제는 템플릿엔진을 사용하고, (서버 side에서 돌아감)
     * 엔티티와 Form 객체의 차이가 거의 없기 때문에
     * 엔티티를 반환한  것이다.
     */
    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
