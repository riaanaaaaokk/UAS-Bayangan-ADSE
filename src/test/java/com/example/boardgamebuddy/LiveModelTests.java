package com.example.boardgamebuddy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("live")
@SpringBootTest
class LiveModelTests {
    @Autowired BoardGameService service;
    @Autowired ChatClient.Builder builder;

    @Test
    void generatedAnswerIsRelevant() {
        var question = new Question("How many dice are used in Yahtzee?");
        var answer = service.askQuestion(question);
        assertThat(answer.answer()).isNotBlank();
        assertThat(new RelevancyEvaluator(builder).evaluate(
                new EvaluationRequest(question.question(), answer.answer())).isPass()).isTrue();
    }

    @Test
    void generatedAnswerIsSupportedByReference() {
        String reference = "Yahtzee is played with five dice.";
        var answer = service.askQuestion(new Question(
                "Based on this reference: " + reference + " How many dice does Yahtzee use?"));
        assertThat(new FactCheckingEvaluator(builder).evaluate(new EvaluationRequest(
                List.of(new Document(reference)), answer.answer())).isPass()).isTrue();
    }
}
