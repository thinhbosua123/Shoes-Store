package com.nhutkhuong1.application.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckPromotion {
    private int discountType;

    private long discountValue;

    private long maximumDiscountValue;
}