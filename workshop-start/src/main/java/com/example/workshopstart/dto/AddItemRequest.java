package com.example.workshopstart.dto;

import java.math.BigDecimal;

public record AddItemRequest(String productId, BigDecimal price, int quantity) {
}
