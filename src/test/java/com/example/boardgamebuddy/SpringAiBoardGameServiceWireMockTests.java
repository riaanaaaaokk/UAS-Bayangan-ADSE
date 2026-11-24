package com.example.boardgamebuddy;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.ai.openai.base-url=${openai.base.url}",
    "spring.ai.openai.api-key=test-only-not-a-real-key",
    "spring.ai.retry.max-attempts=1"
})
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "openai.base.url"))
class SpringAiBoardGameServiceWireMockTests {
    @Autowired BoardGameService service;
    @Autowired MockMvc mvc;

    @BeforeEach
    void stubOpenAi() throws Exception {
        WireMock.reset();
        String json = new ClassPathResource("test-openai-response.json")
                .getContentAsString(StandardCharsets.UTF_8);
        stubFor(WireMock.post(urlEqualTo("/v1/chat/completions"))
                .willReturn(okJson(json)));
    }

    @Test
    void askQuestionReturnsParis() {
        assertThat(service.askQuestion(new Question("What is the capital of France?")))
                .isEqualTo(new Answer("Paris"));
        verify(1, postRequestedFor(urlEqualTo("/v1/chat/completions"))
                .withRequestBody(containing("What is the capital of France?")));
    }

    @Test
    void postAskReturnsJson() throws Exception {
        mvc.perform(post("/ask").contentType("application/json")
                .content("{\"question\":\"What is the capital of France?\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.answer").value("Paris"));
    }

    @Test
    void rejectsInvalidQuestionsBeforeCallingProvider() throws Exception {
        for (String json : new String[]{"{}", "{\"question\":null}", "{\"question\":\"  \"}",
                "{\"question\":\"" + "x".repeat(4001) + "\"}"}) {
            mvc.perform(post("/ask").contentType("application/json").content(json))
                    .andExpect(status().isBadRequest());
        }
        verify(0, postRequestedFor(urlEqualTo("/v1/chat/completions")));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mvc.perform(post("/ask").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest());
    }
}
