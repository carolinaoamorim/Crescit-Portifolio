package com.acessorinvestimentos.portfolio.service;

import com.acessorinvestimentos.portfolio.dto.InvestmentRequest;
import com.acessorinvestimentos.portfolio.exception.DuplicateResourceException;
import com.acessorinvestimentos.portfolio.exception.ResourceNotFoundException;
import com.acessorinvestimentos.portfolio.model.Investment;
import com.acessorinvestimentos.portfolio.model.Portfolio;
import com.acessorinvestimentos.portfolio.repository.ContributionRepository;
import com.acessorinvestimentos.portfolio.repository.InvestmentRepository;
import com.acessorinvestimentos.portfolio.repository.PortfolioRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final ContributionRepository contributionRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioService portfolioService;

    public InvestmentService(InvestmentRepository investmentRepository,
                             ContributionRepository contributionRepository,
                             PortfolioRepository portfolioRepository,
                             PortfolioService portfolioService) {
        this.investmentRepository = investmentRepository;
        this.contributionRepository = contributionRepository;
        this.portfolioRepository = portfolioRepository;
        this.portfolioService = portfolioService;
    }

    @Transactional
    public Investment addInvestment(String userId, InvestmentRequest request) {
        Portfolio portfolio = portfolioService.getOrCreatePortfolio(userId);
        if (investmentRepository.existsByPortfolioIdAndTickerIgnoreCase(portfolio.getId(), request.ticker())) {
            throw new DuplicateResourceException("Investment with ticker " + request.ticker().toUpperCase() + " already exists");
        }
        Investment saved = investmentRepository.save(new Investment(portfolio.getId(), request.ticker(), request.assetName(), request.assetType()));
        portfolio.touch();
        portfolioRepository.save(portfolio);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Investment> getInvestmentsByUserId(String userId) {
        Portfolio portfolio = portfolioService.getPortfolioByUserId(userId);
        return investmentRepository.findAllByPortfolioIdOrderByTicker(portfolio.getId());
    }

    @Transactional(readOnly = true)
    public Investment getInvestmentById(String userId, UUID investmentId) {
        Portfolio portfolio = portfolioService.getPortfolioByUserId(userId);
        return investmentRepository.findByIdAndPortfolioId(investmentId, portfolio.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
    }

    @Transactional
    public Investment updateInvestment(String userId, UUID investmentId, InvestmentRequest request) {
        Portfolio portfolio = portfolioService.getPortfolioByUserId(userId);
        Investment investment = investmentRepository.findByIdAndPortfolioId(investmentId, portfolio.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        if (investmentRepository.existsByPortfolioIdAndTickerIgnoreCaseAndIdNot(portfolio.getId(), request.ticker(), investmentId)) {
            throw new DuplicateResourceException("Investment with ticker " + request.ticker().toUpperCase() + " already exists");
        }
        investment.updateDetails(request.ticker(), request.assetName(), request.assetType());
        portfolio.touch();
        return investmentRepository.save(investment);
    }

    @Transactional
    public void deleteInvestment(String userId, UUID investmentId) {
        Portfolio portfolio = portfolioService.getPortfolioByUserId(userId);
        Investment investment = investmentRepository.findByIdAndPortfolioId(investmentId, portfolio.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        contributionRepository.deleteAllByInvestmentId(investmentId);
        investmentRepository.delete(investment);
        portfolio.touch();
    }
}
