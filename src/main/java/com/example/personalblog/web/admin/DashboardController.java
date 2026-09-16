package com.example.personalblog.web.admin;

import com.example.personalblog.article.Article;
import com.example.personalblog.article.ArticleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final ArticleService articleService;

    public DashboardController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<Article> articles = articleService.listPublished();
        model.addAttribute("articles", articles);
        return "admin/dashboard";
    }
}
