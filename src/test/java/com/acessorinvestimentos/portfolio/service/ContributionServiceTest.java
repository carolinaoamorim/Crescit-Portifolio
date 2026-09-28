package com.acessorinvestimentos.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.acessorinvestimentos.portfolio.dto.ContributionRequest;
import com.acessorinvestimentos.portfolio.exception.ResourceNotFoundException;
import com.acessorinvestimentos.portfolio.model.AssetType;
import com.acessorinvestimentos.portfolio.model.Contribution;
import com.acessorinvestimentos.portfolio.model.Investment;
import com.acessorinvestimentos.portfolio.model.Portfolio;
import com.acessorinvestimentos.portfolio.repository.ContributionRepository;
import com.acessorinvestimentos.portfolio.repository.InvestmentRepository;
import com.acessorinvestimentos.portfolio.repository.PortfolioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
class ContributionServiceTest {

    @Mock ContributionRepository contributionRepository;
    @Mock InvestmentRepository investmentRepository;
    @Mock PortfolioRepository portfolioRepository;
    @Mock PortfolioService portfolioService;
    @InjectMocks ContributionService contributionService;

    private Portfolio portfolio;
    private Investment investment;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio("auth0|123");
        investment = new Investment(portfolio.getId(), "PETR4", "Petrobras", AssetType.STOCK);
    }

    @Test
    void shouldAddContributionAndRecalculateWeightedAverage() {
        Contribution existing = new Contribution(investment.getId(), new BigDecimal("2"),
                new BigDecimal("10.00"), LocalDate.now().minusDays(1));
        Contribution added = new Contribution(investment.getId(), new BigDecimal("3"),
                new BigDecimal("20.00"), LocalDate.now());
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));
        when(investmentRepository.findWithLockById(investment.getId())).thenReturn(Optional.of(investment));
        when(contributionRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Contribution.class))).thenReturn(added);
        when(contributionRepository.findAllByInvestmentIdOrderByContributionDateDescCreatedAtDesc(investment.getId()))
                .thenReturn(List.of(existing, added));
        when(investmentRepository.save(investment)).thenReturn(investment);
        when(portfolioRepository.findById(portfolio.getId())).thenReturn(Optional.of(portfolio));

        Contribution result = contributionService.addContribution("auth0|123", investment.getId(),
                new ContributionRequest(new BigDecimal("3"), new BigDecimal("20"), LocalDate.now()));

        assertEquals(new BigDecimal("60.00"), result.getAmount());
        assertEquals(new BigDecimal("5"), investment.getTotalQuantity());
        assertEquals(new BigDecimal("80.00"), investment.getTotalInvested());
        assertEquals(new BigDecimal("16.00000000"), investment.getAveragePrice());
    }

    @Test
    void shouldResetTotalsAfterLastContributionIsDeleted() {
        Contribution contribution = new Contribution(investment.getId(), BigDecimal.ONE,
                new BigDecimal("25"), LocalDate.now());
        investment.updateTotals(BigDecimal.ONE, new BigDecimal("25.00"), new BigDecimal("25.00000000"));
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));
        when(investmentRepository.findWithLockById(investment.getId())).thenReturn(Optional.of(investment));
        when(contributionRepository.findByIdAndInvestmentId(contribution.getId(), investment.getId()))
                .thenReturn(Optional.of(contribution));
        when(contributionRepository.findAllByInvestmentIdOrderByContributionDateDescCreatedAtDesc(investment.getId()))
                .thenReturn(List.of());
        when(portfolioRepository.findById(portfolio.getId())).thenReturn(Optional.of(portfolio));

        contributionService.deleteContribution("auth0|123", investment.getId(), contribution.getId());

        assertEquals(0, investment.getTotalQuantity().signum());
        assertEquals(0, investment.getTotalInvested().signum());
        assertEquals(0, investment.getAveragePrice().signum());
    }

    @Test
    void shouldListContributionsFromOwnedInvestment() {
        Contribution contribution = new Contribution(investment.getId(), BigDecimal.ONE,
                new BigDecimal("25"), LocalDate.now());
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));
        when(contributionRepository.findAllByInvestmentIdOrderByContributionDateDescCreatedAtDesc(investment.getId()))
                .thenReturn(List.of(contribution));

        List<Contribution> result = contributionService.getContributions("auth0|123", investment.getId());

        assertEquals(1, result.size());
        assertSame(contribution, result.get(0));
    }

    @Test
    void shouldRecalculateInvestmentDirectly() {
        Contribution contribution = new Contribution(investment.getId(), new BigDecimal("4"),
                new BigDecimal("12.50"), LocalDate.now());
        when(investmentRepository.findWithLockById(investment.getId())).thenReturn(Optional.of(investment));
        when(contributionRepository.findAllByInvestmentIdOrderByContributionDateDescCreatedAtDesc(investment.getId()))
                .thenReturn(List.of(contribution));
        when(investmentRepository.save(investment)).thenReturn(investment);

        Investment result = contributionService.recalculateInvestment(investment.getId());

        assertEquals(new BigDecimal("4"), result.getTotalQuantity());
        assertEquals(new BigDecimal("50.00"), result.getTotalInvested());
        assertEquals(new BigDecimal("12.50000000"), result.getAveragePrice());
    }

    @Test
    void shouldFailWhenRecalculatingMissingInvestment() {
        when(investmentRepository.findWithLockById(investment.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contributionService.recalculateInvestment(investment.getId()));
    }

    @Test
    void shouldFailWhenContributionDoesNotExist() {
        UUID contributionId = UUID.randomUUID();
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));
        when(investmentRepository.findWithLockById(investment.getId())).thenReturn(Optional.of(investment));
        when(contributionRepository.findByIdAndInvestmentId(contributionId, investment.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contributionService.deleteContribution("auth0|123", investment.getId(), contributionId));
    }

    @Test
    void shouldFailWhenInvestmentDoesNotBelongToUser() {
        when(portfolioService.getPortfolioByUserId("auth0|other")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contributionService.getContributions("auth0|other", investment.getId()));
    }

    @Test
    void shouldFailWhenPortfolioDisappearsDuringContribution() {
        Contribution added = new Contribution(investment.getId(), BigDecimal.ONE,
                BigDecimal.TEN, LocalDate.now());
        when(portfolioService.getPortfolioByUserId("auth0|123")).thenReturn(portfolio);
        when(investmentRepository.findByIdAndPortfolioId(investment.getId(), portfolio.getId()))
                .thenReturn(Optional.of(investment));
        when(investmentRepository.findWithLockById(investment.getId())).thenReturn(Optional.of(investment));
        when(contributionRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(Contribution.class))).thenReturn(added);
        when(contributionRepository.findAllByInvestmentIdOrderByContributionDateDescCreatedAtDesc(investment.getId()))
                .thenReturn(List.of(added));
        when(investmentRepository.save(investment)).thenReturn(investment);
        when(portfolioRepository.findById(portfolio.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contributionService.addContribution("auth0|123", investment.getId(),
                        new ContributionRequest(BigDecimal.ONE, BigDecimal.TEN, LocalDate.now())));
    }
}
