package com.example.personalblog.web;

import com.example.personalblog.article.Article;
import com.example.personalblog.article.ArticleService;
import com.example.personalblog.markdown.MarkdownRenderer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ArticleController {

    private final ArticleService articleService;
    private final MarkdownRenderer markdownRenderer;

    public ArticleController(ArticleService articleService, MarkdownRenderer markdownRenderer) {
        this.articleService = articleService;
        this.markdownRenderer = markdownRenderer;
    }

    @GetMapping("/article/{id}")
    public String detail(@PathVariable String id, Model model) {
        Article article = articleService.getById(id);
        model.addAttribute("article", article);
        model.addAttribute("contentHtml", markdownRenderer.toHtml(article.content()));
        return "article-detail";
    }
}
