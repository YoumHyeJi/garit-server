package com.garit.study.domain.item;

import com.garit.study.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity

// dtype 컬럼에 "M"이 들어감
@DiscriminatorValue("M")

@Getter
@Setter
public class Movie extends Item {

    private String director;

    private String actor;
}
