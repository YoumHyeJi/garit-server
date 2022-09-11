package com.garit.study.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity

// dtype 컬럼에 "B"가 들어감
@DiscriminatorValue("B")

@Getter
@Setter
public class Book extends Item {

    private String author;

    private String isbn;
}
