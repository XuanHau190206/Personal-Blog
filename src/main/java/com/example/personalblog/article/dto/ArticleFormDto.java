package com.example.personalblog.article.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ArticleFormDto(
        @NotBlank(message = "Tiêu đề không được để trống") String title,
        LocalDate publishedDate,
        @NotBlank(message = "Nội dung không được để trống") String content
) {
}
