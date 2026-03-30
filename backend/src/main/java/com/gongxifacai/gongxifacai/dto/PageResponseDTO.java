package com.gongxifacai.gongxifacai.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponseDTO<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static <T> PageResponseDTO<T> from(Page<T> springPage) {
        PageResponseDTO<T> dto = new PageResponseDTO<>();
        dto.setContent(springPage.getContent());
        dto.setTotalElements(springPage.getTotalElements());
        dto.setTotalPages(springPage.getTotalPages());
        dto.setPage(springPage.getNumber());
        dto.setSize(springPage.getSize());
        return dto;
    }
}

