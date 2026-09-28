package com.acessorinvestimentos.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acessorinvestimentos.portfolio.dto.InvestmentRequest;
import com.acessorinvestimentos.portfolio.exception.DuplicateResourceException;
import com.acessorinvestimentos.portfolio.exception.ResourceNotFoundException;
import com.acessorinvestimentos.portfolio.model.AssetType;
import com.acessorinvestimentos.portfolio.model.Investment;
import com.acessorinvestimentos.portfolio.model.Portfolio;
import com.acessorinvestimentos.portfolio.repository.ContributionRepository;
import com.acessorinvestimentos.portfolio.repository.InvestmentRepository;
import com.acessorinvestimentos.portfolio.repository.PortfolioRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock InvestmentRepository investmentRepository;
    @Mock ContributionRepository contributionRepository;
    @Mock PortfolioRepository portfolioRepository;
    @Mock PortfolioService portfolioService;
    @InjectMocks InvestmentService investmentService;

    private Portfolio portfolio;
    private InvestmentRequest request;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio("auth0|123");
        request = new InvestmentRequest("petr4", "Petrobras", AssetType.STOCK);
    }

    @Test
    void shouldAddInvestmentAndNormalizeTicker() {
        when(portfolioService.getOrCreatePortfolio("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.existsByPortfolioIdAndTickerIgnoreCase(portfolio.getId(), "petr4")).thenReturn(false);
        when(investmentRepository.save(org.mockito.ArgumentMatchers.any(Investment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Investment result = investmentService.addInvestment("auth0|123", request);

        assertEquals("PETR4", result.getTicker());
        assertEquals(0, result.getTotalInvested().signum());
    }

    @Test
    void shouldRejectDuplicateTicker() {
        when(portfolioService.getOrCreatePortfolio("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.existsByPortfolioIdAndTickerIgnoreCase(portfolio.getId(), "petr4")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> investmentService.addInvestment("auth0|123", request));
    }

    @Test
    void shouldNotReturnInvestmentFromAnotherPortfolio() {
        UUID investmentId = UUID.randomUUID();
        when(portfolioService.getPortfolioByUserId("auth0|other")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investmentId, portfolio.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> investmentService.getInvestmentById("auth0|other", investmentId));
    }

    @Test
    void shouldListInvestmentsFromUserPortfolio() {
        Investment investment = new Investment(portfolio.getId(), "PETR4", "Petrobras", AssetType.STOCK);
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findAllByPortfolioIdOrderByTicker(portfolio.getId()))
                .thenReturn(List.of(investment));

        List<Investment> result = investmentService.getInvestmentsByUserId("auth0|123");

        assertEquals(1, result.size());
        assertSame(investment, result.get(0));
    }

    @Test
    void shouldReturnOwnedInvestment() {
        Investment investment = new Investment(portfolio.getId(), "PETR4", "Petrobras", AssetType.STOCK);
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));

        assertSame(investment, investmentService.getInvestmentById("auth0|123", investment.getId()));
    }

    @Test
    void shouldUpdateInvestmentDetails() {
        Investment investment = new Investment(portfolio.getId(), "PETR4", "Petrobras", AssetType.STOCK);
        InvestmentRequest update = new InvestmentRequest("ivvb11", "ETF S&P 500", AssetType.ETF);
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));
        when(investmentRepository.existsByPortfolioIdAndTickerIgnoreCaseAndIdNot(
                portfolio.getId(), "ivvb11", investment.getId())).thenReturn(false);
        when(investmentRepository.save(investment)).thenReturn(investment);

        Investment result = investmentService.updateInvestment("auth0|123", investment.getId(), update);

        assertEquals("IVVB11", result.getTicker());
        assertEquals("ETF S&P 500", result.getAssetName());
        assertEquals(AssetType.ETF, result.getAssetType());
    }

    @Test
    void shouldRejectDuplicateTickerWhenUpdating() {
        Investment investment = new Investment(portfolio.getId(), "PETR4", "Petrobras", AssetType.STOCK);
        InvestmentRequest update = new InvestmentRequest("VALE3", "Vale", AssetType.STOCK);
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));
        when(investmentRepository.existsByPortfolioIdAndTickerIgnoreCaseAndIdNot(
                portfolio.getId(), "VALE3", investment.getId())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> investmentService.updateInvestment("auth0|123", investment.getId(), update));
    }

    @Test
    void shouldDeleteInvestmentAndItsContributions() {
        Investment investment = new Investment(portfolio.getId(), "PETR4", "Petrobras", AssetType.STOCK);
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));

        investmentService.deleteInvestment("auth0|123", investment.getId());

        verify(contributionRepository).deleteAllByInvestmentId(investment.getId());
        verify(investmentRepository).delete(investment);
    }

    @Test
    void shouldFailWhenUpdatingMissingInvestment() {
        UUID investmentId = UUID.randomUUID();
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investmentId, portfolio.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> investmentService.updateInvestment("auth0|123", investmentId, request));
    }
}
