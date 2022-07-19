package com.garit.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication 어노테이션이 붙으면,
 * 해당 패키지 하위에 있는 클래스를 모두 component scan해서 스프링 빈에 자동 등록한다.
 */
@SpringBootApplication
public class StudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudyApplication.class, args);
	}

}
