package com.example.personalblog.web;

import com.example.personalblog.article.Article;
import com.example.personalblog.article.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final ArticleService articleService;

    public HomeController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Article> articles = articleService.listPublished();
        model.addAttribute("articles", articles);
        return "home";
    }
}
