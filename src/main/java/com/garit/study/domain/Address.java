package com.garit.study.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

// JPA 내장 타입
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;
}
