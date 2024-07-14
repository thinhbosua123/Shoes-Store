package com.nhutkhuong1.application.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageableDTO {
    private Object items;

    private int totalPages;

    private int currentPage;
}
