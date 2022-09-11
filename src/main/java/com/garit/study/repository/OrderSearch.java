package com.garit.study.repository;

import com.garit.study.domain.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearch {

    private String memberName;          // 회원 이름
    private OrderStatus orderStatus;    // 주문 상태 [ORDER, CANCEL]
}
