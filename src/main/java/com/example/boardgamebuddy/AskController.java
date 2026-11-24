package com.example.boardgamebuddy;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {
    private final BoardGameService service;

    public AskController(BoardGameService service) {
        this.service = service;
    }

    @PostMapping("/ask")
    public Answer ask(@Valid @RequestBody Question question) {
        return service.askQuestion(question);
    }
}
