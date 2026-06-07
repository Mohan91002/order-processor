package com.learning.orderprocessor.dto;

import com.learning.orderprocessor.validation.PositiveQuantity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Email String customerEmail,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(
            @NotNull Long productId,
            @PositiveQuantity Integer quantity
    ) {}
}
