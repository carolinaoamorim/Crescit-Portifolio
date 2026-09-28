package com.acessorinvestimentos.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acessorinvestimentos.portfolio.exception.ResourceNotFoundException;
import com.acessorinvestimentos.portfolio.model.AssetType;
import com.acessorinvestimentos.portfolio.model.Investment;
import com.acessorinvestimentos.portfolio.model.Portfolio;
import com.acessorinvestimentos.portfolio.repository.InvestmentRepository;
import com.acessorinvestimentos.portfolio.repository.PortfolioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock PortfolioRepository portfolioRepository;
    @Mock InvestmentRepository investmentRepository;
    @InjectMocks PortfolioService portfolioService;

    @Test
    void shouldReturnExistingPortfolio() {
        Portfolio portfolio = new Portfolio("auth0|123");
        when(portfolioRepository.findByUserId("auth0|123")).thenReturn(Optional.of(portfolio));

        Portfolio result = portfolioService.getOrCreatePortfolio("auth0|123");

        assertSame(portfolio, result);
    }

    @Test
    void shouldCreatePortfolioWhenItDoesNotExist() {
        when(portfolioRepository.findByUserId("auth0|123")).thenReturn(Optional.empty());
        when(portfolioRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Portfolio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Portfolio result = portfolioService.getOrCreatePortfolio("auth0|123");

        assertEquals("auth0|123", result.getUserId());
        verify(portfolioRepository).saveAndFlush(org.mockito.ArgumentMatchers.any(Portfolio.class));
    }

    @Test
    void shouldCalculateTotalInvested() {
        Portfolio portfolio = new Portfolio("auth0|123");
        Investment first = new Investment(portfolio.getId(), "PETR4", "Petrobras", AssetType.STOCK);
        Investment second = new Investment(portfolio.getId(), "MXRF11", "Maxi Renda", AssetType.FII);
        first.updateTotals(BigDecimal.ONE, new BigDecimal("100.50"), new BigDecimal("100.50"));
        second.updateTotals(BigDecimal.TEN, new BigDecimal("80.25"), new BigDecimal("8.025"));
        when(portfolioRepository.findByUserId("auth0|123")).thenReturn(Optional.of(portfolio));
        when(investmentRepository.findAllByPortfolioIdOrderByTicker(portfolio.getId()))
                .thenReturn(List.of(first, second));

        assertEquals(new BigDecimal("180.75"), portfolioService.calculateTotalInvested("auth0|123"));
    }

    @Test
    void shouldFailWhenPortfolioDoesNotExist() {
        when(portfolioRepository.findByUserId("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> portfolioService.getPortfolioByUserId("missing"));
    }

    @Test
    void shouldReturnPortfolioCreatedByConcurrentRequest() {
        Portfolio concurrentPortfolio = new Portfolio("auth0|123");
        when(portfolioRepository.findByUserId("auth0|123"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(concurrentPortfolio));
        when(portfolioRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Portfolio.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate user"));

        assertSame(concurrentPortfolio, portfolioService.getOrCreatePortfolio("auth0|123"));
    }
}
