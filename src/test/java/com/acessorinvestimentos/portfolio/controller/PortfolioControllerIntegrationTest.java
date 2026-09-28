package com.acessorinvestimentos.portfolio.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PortfolioControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void shouldCompletePortfolioFlowAndRecalculateAfterDelete() throws Exception {
        String userId = "auth0|integration-user";
        String investmentJson = """
                {"ticker":"PETR4","assetName":"Petrobras","assetType":"STOCK"}
                """;

        String investmentBody = mockMvc.perform(post("/portfolios/{userId}/investments", userId)
                        .contentType(MediaType.APPLICATION_JSON).content(investmentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("PETR4"))
                .andReturn().getResponse().getContentAsString();
        JsonNode investment = objectMapper.readTree(investmentBody);
        String investmentId = investment.get("id").asText();

        String firstContribution = mockMvc.perform(post(
                        "/portfolios/{userId}/investments/{investmentId}/contributions", userId, investmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2,\"unitPrice\":10,\"contributionDate\":\"2026-01-10\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(20.00))
                .andReturn().getResponse().getContentAsString();
        String firstContributionId = objectMapper.readTree(firstContribution).get("id").asText();

        mockMvc.perform(post("/portfolios/{userId}/investments/{investmentId}/contributions", userId, investmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3,\"unitPrice\":20,\"contributionDate\":\"2026-01-11\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/portfolios/{userId}/investments/{investmentId}", userId, investmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(5))
                .andExpect(jsonPath("$.totalInvested").value(80.00))
                .andExpect(jsonPath("$.averagePrice").value(16.0));

        mockMvc.perform(get("/portfolios/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvested").value(80.00));

        mockMvc.perform(delete("/portfolios/{userId}/investments/{investmentId}/contributions/{contributionId}",
                        userId, investmentId, firstContributionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/portfolios/{userId}/investments/{investmentId}", userId, investmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(3))
                .andExpect(jsonPath("$.totalInvested").value(60.00))
                .andExpect(jsonPath("$.averagePrice").value(20.0));
    }

    @Test
    void shouldValidateRequestAndRejectDuplicateTicker() throws Exception {
        String userId = "auth0|validation-user";
        String investmentJson = """
                {"ticker":"IVVB11","assetName":"ETF S&P 500","assetType":"ETF"}
                """;
        mockMvc.perform(post("/portfolios/{userId}/investments", userId)
                        .contentType(MediaType.APPLICATION_JSON).content(investmentJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/portfolios/{userId}/investments", userId)
                        .contentType(MediaType.APPLICATION_JSON).content(investmentJson))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/portfolios/{userId}/investments", "auth0|invalid-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"\",\"assetName\":\"\",\"assetType\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.ticker").exists())
                .andExpect(jsonPath("$.fieldErrors.assetName").exists())
                .andExpect(jsonPath("$.fieldErrors.assetType").exists());
    }

    @Test
    void shouldHideInvestmentOwnedByAnotherUser() throws Exception {
        String owner = "auth0|owner";
        String investmentBody = mockMvc.perform(post("/portfolios/{userId}/investments", owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"CDB\",\"assetName\":\"CDB Banco\",\"assetType\":\"FIXED_INCOME\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String investmentId = objectMapper.readTree(investmentBody).get("id").asText();

        mockMvc.perform(post("/portfolios/{userId}/investments", "auth0|other")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ticker\":\"BOVA11\",\"assetName\":\"Ibovespa ETF\",\"assetType\":\"ETF\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/portfolios/{userId}/investments/{investmentId}", "auth0|other", investmentId))
                .andExpect(status().isNotFound());
    }
}
