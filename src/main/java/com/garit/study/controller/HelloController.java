package com.garit.study.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("hello")
    public String hello(Model model) {
        /**
         * Controller에서 View로 model을 넘길 수 있다.
         * model은 key와 value 쌍으로 이루어진 데이터
         */
        model.addAttribute("data", "hello!");

        /**
         * resources/templates 폴더에 있는 화면 이름
         * 자동으로 이름 뒤에 .html이 붙음
         */
        return "hello";
    }
}
