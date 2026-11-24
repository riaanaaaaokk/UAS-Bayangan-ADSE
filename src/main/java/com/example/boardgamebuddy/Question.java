package com.example.boardgamebuddy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Question(@NotBlank @Size(max = 4000) String question) {}
