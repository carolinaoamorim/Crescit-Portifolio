package com.acessorinvestimentos.portfolio.service;

import com.acessorinvestimentos.portfolio.dto.ContributionRequest;
import com.acessorinvestimentos.portfolio.exception.ResourceNotFoundException;
import com.acessorinvestimentos.portfolio.model.Contribution;
import com.acessorinvestimentos.portfolio.model.Investment;
import com.acessorinvestimentos.portfolio.model.Portfolio;
import com.acessorinvestimentos.portfolio.repository.ContributionRepository;
import com.acessorinvestimentos.portfolio.repository.InvestmentRepository;
import com.acessorinvestimentos.portfolio.repository.PortfolioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final InvestmentRepository investmentRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioService portfolioService;

    public ContributionService(ContributionRepository contributionRepository,
                               InvestmentRepository investmentRepository,
                               PortfolioRepository portfolioRepository,
                               PortfolioService portfolioService) {
        this.contributionRepository = contributionRepository;
        this.investmentRepository = investmentRepository;
        this.portfolioRepository = portfolioRepository;
        this.portfolioService = portfolioService;
    }

    @Transactional
    public Contribution addContribution(String userId, UUID investmentId, ContributionRequest request) {
        Investment investment = getOwnedInvestmentForUpdate(userId, investmentId);
        Contribution contribution = contributionRepository.saveAndFlush(new Contribution(
                investmentId, request.quantity(), request.unitPrice(), request.contributionDate()));
        recalculateInvestment(investment);
        touchPortfolio(investment.getPortfolioId());
        return contribution;
    }

    @Transactional(readOnly = true)
    public List<Contribution> getContributions(String userId, UUID investmentId) {
        getOwnedInvestment(userId, investmentId);
        return contributionRepository.findAllByInvestmentIdOrderByContributionDateDescCreatedAtDesc(investmentId);
    }

    @Transactional
    public void deleteContribution(String userId, UUID investmentId, UUID contributionId) {
        Investment investment = getOwnedInvestmentForUpdate(userId, investmentId);
        Contribution contribution = contributionRepository.findByIdAndInvestmentId(contributionId, investmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));
        contributionRepository.delete(contribution);
        contributionRepository.flush();
        recalculateInvestment(investment);
        touchPortfolio(investment.getPortfolioId());
    }

    @Transactional
    public Investment recalculateInvestment(UUID investmentId) {
        Investment investment = investmentRepository.findWithLockById(investmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        return recalculateInvestment(investment);
    }

    private Investment recalculateInvestment(Investment investment) {
        List<Contribution> contributions = contributionRepository
                .findAllByInvestmentIdOrderByContributionDateDescCreatedAtDesc(investment.getId());
        BigDecimal quantity = contributions.stream().map(Contribution::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal invested = contributions.stream().map(Contribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal average = quantity.signum() == 0
                ? BigDecimal.ZERO
                : invested.divide(quantity, 8, RoundingMode.HALF_UP);
        investment.updateTotals(quantity, invested, average);
        return investmentRepository.save(investment);
    }

    private Investment getOwnedInvestment(String userId, UUID investmentId) {
        Portfolio portfolio = portfolioService.getPortfolioByUserId(userId);
        return investmentRepository.findByIdAndPortfolioId(investmentId, portfolio.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
    }

    private Investment getOwnedInvestmentForUpdate(String userId, UUID investmentId) {
        Investment owned = getOwnedInvestment(userId, investmentId);
        return investmentRepository.findWithLockById(owned.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
    }

    private void touchPortfolio(UUID portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));
        portfolio.touch();
        portfolioRepository.save(portfolio);
    }
}
