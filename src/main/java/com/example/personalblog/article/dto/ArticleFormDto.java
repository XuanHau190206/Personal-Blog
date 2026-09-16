package com.example.personalblog.article.dto;

import jakarta.validation.constraints.NotBlank;

public record ArticleFormDto(
        @NotBlank(message = "Tiêu đề không được để trống") String title,
        @NotBlank(message = "Nội dung không được để trống") String content
) {
}
