package com.garit.study.controller;

import com.garit.study.domain.item.Book;
import com.garit.study.domain.item.Item;
import com.garit.study.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model){
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    /**
     * createBook() 메서드를 만들어서 파라미터를 넘기는게 더 좋은 설계이다.
     * 즉, setter 사용을 지양해야 한다.
     */
    @PostMapping("/items/new")
    public String create(@Valid BookForm form, BindingResult result){
        if(result.hasErrors()){
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
    public String list(Model model){
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }
}
