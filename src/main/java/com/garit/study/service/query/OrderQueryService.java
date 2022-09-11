package com.garit.study.service.query;

import com.garit.study.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 화면에 맞춘 쿼리 전용 Service
 * => 핵심 Service 비즈니스와, 단순 화면용(쿼리용) Service를 분리하는게 좋다.
 *
 * 1) 트랜잭션 안에서 동작하는 쿼리 전용 Service를 만든다.
 *    => 트랜잭션 안에서 동작하기 때문에 OSIV를 꺼도 lazy loading 관련 exception이 발생하지 않는다.
 * 2) Controller에서 Lazy Loading을 호출하는 코드를 쿼리 전용 Service로 옮긴다.
 */
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;


}
