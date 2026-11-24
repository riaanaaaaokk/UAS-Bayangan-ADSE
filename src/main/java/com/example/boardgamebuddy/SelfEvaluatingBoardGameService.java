package com.example.boardgamebuddy;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.self-evaluation", havingValue = "true")
public class SelfEvaluatingBoardGameService implements BoardGameService {
    private final ChatClient chatClient;
    private final RelevancyEvaluator evaluator;

    public SelfEvaluatingBoardGameService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
        this.evaluator = new RelevancyEvaluator(builder);
    }

    @Override
    @Retryable(retryFor = AnswerNotRelevantException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 100))
    public Answer askQuestion(Question question) {
        String answer = chatClient.prompt().user(question.question()).call().content();
        var result = evaluator.evaluate(new EvaluationRequest(question.question(), answer));
        if (!result.isPass()) {
            throw new AnswerNotRelevantException(question.question(), answer);
        }
        return new Answer(answer);
    }

    @Recover
    public Answer recover(AnswerNotRelevantException exception, Question question) {
        return new Answer("I'm sorry, I wasn't able to answer the question.");
    }
}
