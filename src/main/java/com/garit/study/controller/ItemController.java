package com.garit.study.controller;

import com.garit.study.domain.item.Book;
import com.garit.study.domain.item.Item;
import com.garit.study.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    /**
     * createBook() 메서드를 만들어서 파라미터를 넘기는게 더 좋은 설계이다.
     * 즉, setter 사용을 지양해야 한다.
     */
    @PostMapping("/items/new")
    public String create(@Valid BookForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "items/createItemForm";
        }

        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    // Model을 통해 View에 Book 엔티티를 보내는 것이 아니라, BookForm을 보낸다.
    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);

        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    /**
     * book은 DB에서 식별자(id)를 한번 조회한 이력이 있는 준영속 상태 객체이다.
     *
     * 즉, DB를 조회함으로써 JPA가 식별할 수 있는 id값을 가지고 있지만,
     * 영속성 컨텍스트에서 더이상 관리되지 않는 객체를 준영속 엔티티라고 한다.
     * 따라서 준영속 엔티티에 대한 변경 감지(dirty checking)은 일어나지 않는다.
     *
     * [준영속 엔티티를 수정하는 2가지 방법]
     * 1. 변경 감지(dirty checking) 기능 사용
     * 2. 병합(merge) 사용
     *
     * cf) 반면 영속 엔티티는 JPA의 영속성 컨텍스트에서 관리된다.
     * 따라서 변경 감지(dirty checking)이 일어난다.
     */
    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable("itemId") Long itemId, @ModelAttribute("form") BookForm form) {

        /*
        Book book = new Book();
        book.setId(form.getId());
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());
        itemService.saveItem(book);
        */

        /**
         * 컨트롤러에서 어설프게 엔티티를 생성하면 안된다 !!
         *
         * 파라미터로 직접 넘기면, 코드가 딱 매칭되므로 유지보수성이 좋다.
         * 만약 파라미터가 너무 많으면, Service 계층에 DTO를 만들면 된다.
         * => 트랜잭션이 있는 서비스 계층에 식별자(id)와 변경할 데이터를 명확하게 전달하라. (파라미터 or DTO)
         * => "트랜잭션이 있는 서비스 계층"에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하라.
         * => 이를 통해 트랜잭션 커밋 시점에 변경 감지가 실행된다.
         *
         * !! 트랜잭션 안에서 엔티티를 조회해야 영속 상태의 엔티티를 얻을 수 있다.
         * !! 영속 상태의 엔티티를 변경해야, 커밋 시점에 flush가 일어나면서 변경감지가 수행된다.
         */
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

        return "redirect:/items";
    }

}
