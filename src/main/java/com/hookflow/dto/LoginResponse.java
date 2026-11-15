package com.hookflow.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}