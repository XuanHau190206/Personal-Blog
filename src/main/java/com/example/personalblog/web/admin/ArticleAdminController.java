package com.example.personalblog.web.admin;

import com.example.personalblog.article.ArticleService;
import com.example.personalblog.article.dto.ArticleFormDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/articles")
public class ArticleAdminController {

    private final ArticleService articleService;

    public ArticleAdminController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("articleForm", new ArticleFormDto(null, null, null));
        return "admin/article-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("articleForm") ArticleFormDto form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/article-form";
        }
        articleService.create(form);
        return "redirect:/admin";
    }
}
