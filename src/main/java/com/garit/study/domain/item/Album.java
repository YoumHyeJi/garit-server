package com.garit.study.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity

// dtype 컬럼에 "A"가 들어감
@DiscriminatorValue("A")

@Getter
@Setter
public class Album extends Item {

    private String artist;

    private String etc;
}
