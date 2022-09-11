package com.garit.study.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

/**
 * JPA 내장 타입 = 값 타입
 * 값 타입은 immutable(변경 불가) 해야 한다. => 값 타입은 변경 불가능하게 설계해야 한다.
 * 즉, 생성할 때만 값이 셋팅되어야 한다.
 * 따라서 Getter만 제공!! (Setter 제공 X)
 */
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    /**
     * 리플랙션이나 프록시 같은 기술을 써야하기 때문에, 기본 생성자가 필요하다.
     * 대신 protected를 붙여서, 기본 생성자를 new를 통해 자유롭게 호출하지 못하도록 한다.
     * => Address 클래스를 상속할 일이 거의 없기 때문!
     */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
