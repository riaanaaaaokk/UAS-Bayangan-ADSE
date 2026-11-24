package com.example.boardgamebuddy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.ai.openai.base-url=${openai.base.url}",
    "spring.ai.openai.api-key=test-only-not-a-real-key",
    "app.self-evaluation=true"
})
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "openai.base.url"))
class EvaluationWireMockTests {
    @Autowired ChatClient.Builder builder;
    @Autowired BoardGameService service;

    @BeforeEach
    void resetServer() { reset(); }

    private void reply(String content) {
        stubFor(post(urlEqualTo("/v1/chat/completions")).willReturn(okJson(response(content))));
    }

    private String response(String content) {
        return "{\"id\":\"test\",\"object\":\"chat.completion\",\"created\":1745182545,"
                + "\"model\":\"gpt-4o-mini\",\"choices\":[{\"index\":0,\"message\":{"
                + "\"role\":\"assistant\",\"content\":\"" + content
                + "\"},\"finish_reason\":\"stop\"}]}";
    }

    @Test
    void relevancyPassesAndFailsAccordingToJudge() {
        var request = new EvaluationRequest("How many dice in Yahtzee?", "Five dice.");
        var evaluator = new RelevancyEvaluator(builder);
        reply("YES");
        assertThat(evaluator.evaluate(request).isPass()).isTrue();
        reply("NO");
        assertThat(evaluator.evaluate(request).isPass()).isFalse();
        verify(2, postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withRequestBody(containing("How many dice in Yahtzee?"))
                .withRequestBody(containing("Five dice.")));
    }

    @Test
    void factCheckingUsesReferenceDocumentAndRejectsUnsupportedClaim() {
        var request = new EvaluationRequest(List.of(new Document("Yahtzee uses five dice.")),
                "Yahtzee uses six dice.");
        var evaluator = new FactCheckingEvaluator(builder);
        reply("no");
        assertThat(evaluator.evaluate(request).isPass()).isFalse();
        reply("yes");
        assertThat(evaluator.evaluate(new EvaluationRequest(request.getDataList(),
                "Yahtzee uses five dice.")).isPass()).isTrue();
        verify(2, postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withRequestBody(containing("Yahtzee uses five dice.")));
    }

    private void sequence(String... contents) {
        String state = "Started";
        for (int i = 0; i < contents.length; i++) {
            String next = "step-" + (i + 1);
            stubFor(post(urlEqualTo("/v1/chat/completions"))
                    .inScenario("retry").whenScenarioStateIs(state)
                    .willSetStateTo(next).willReturn(okJson(response(contents[i]))));
            state = next;
        }
    }

    @Test
    void runtimeReturnsAcceptedAnswerWithoutRetry() {
        sequence("Five dice.", "YES");
        assertThat(service.askQuestion(new Question("How many dice?")))
                .isEqualTo(new Answer("Five dice."));
        verify(2, postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }

    @Test
    void runtimeRetriesIrrelevantAnswerThenSucceeds() {
        sequence("Paris", "NO", "Five dice.", "YES");
        assertThat(service.askQuestion(new Question("How many dice?")))
                .isEqualTo(new Answer("Five dice."));
        verify(4, postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }

    @Test
    void runtimeRecoversAfterThreeFailedAttempts() {
        sequence("Paris", "NO", "Paris", "NO", "Paris", "NO");
        assertThat(service.askQuestion(new Question("How many dice?")))
                .isEqualTo(new Answer("I'm sorry, I wasn't able to answer the question."));
        verify(6, postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }

    @Test
    void transportFailureIsNotDisguisedAsRelevancyFailure() {
        stubFor(post(urlEqualTo("/v1/chat/completions")).willReturn(aResponse().withStatus(401)));
        assertThatThrownBy(() -> service.askQuestion(new Question("How many dice?")))
                .isInstanceOf(RuntimeException.class)
                .isNotInstanceOf(AnswerNotRelevantException.class);
        verify(1, postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }
}
