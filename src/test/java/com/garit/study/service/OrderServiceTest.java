package com.garit.study.service;

import com.garit.study.domain.Address;
import com.garit.study.domain.Member;
import com.garit.study.domain.Order;
import com.garit.study.domain.OrderStatus;
import com.garit.study.domain.item.Book;
import com.garit.study.domain.item.Item;
import com.garit.study.exception.NotEnoughStockException;
import com.garit.study.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(getOrder.getStatus())
                .isEqualTo(OrderStatus.ORDER)
                .as("상품 주문시 상태는 ORDER");

        assertThat(getOrder.getOrderItems().size())
                .isEqualTo(1)
                .as("주문한 상품 종류 수가 정확해야 한다.");

        assertThat(getOrder.getTotalPrice())
                .isEqualTo(10000 * orderCount)
                .as("주문 가격은 가격 * 수량이다.");

        assertThat(book.getStockQuantity())
                .isEqualTo(10 - orderCount)
                .as("주문 수량만큼 재고가 줄어야 한다.");
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when
        Assertions.assertThatCode(()->{
            orderService.order(member.getId(), item.getId(), orderCount);}
        ).isInstanceOf(NotEnoughStockException.class);

    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book item = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertThat(getOrder.getStatus())
                .isEqualTo(OrderStatus.CANCEL)
                .as("주문 취소시 상태는 CANCEL 이다.");
        assertThat(item.getStockQuantity())
                .isEqualTo(10)
                .as("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.");
    }


    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
}