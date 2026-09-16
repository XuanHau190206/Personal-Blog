package com.example.personalblog.web.admin;

import com.example.personalblog.article.Article;
import com.example.personalblog.article.ArticleService;
import com.example.personalblog.article.dto.ArticleFormDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        Article article = articleService.getById(id);
        model.addAttribute("articleForm",
                new ArticleFormDto(article.title(), article.publishedDate(), article.content()));
        model.addAttribute("articleId", id);
        return "admin/article-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id,
                          @Valid @ModelAttribute("articleForm") ArticleFormDto form,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("articleId", id);
            return "admin/article-form";
        }
        articleService.update(id, form);
        return "redirect:/admin";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        articleService.delete(id);
        return "redirect:/admin";
    }
}
