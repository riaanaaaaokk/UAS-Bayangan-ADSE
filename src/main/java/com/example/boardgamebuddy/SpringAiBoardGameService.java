package com.example.boardgamebuddy;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.self-evaluation", havingValue = "false", matchIfMissing = true)
public class SpringAiBoardGameService implements BoardGameService {
    private final ChatClient chatClient;

    public SpringAiBoardGameService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Answer askQuestion(Question question) {
        return new Answer(chatClient.prompt().user(question.question()).call().content());
    }
}
