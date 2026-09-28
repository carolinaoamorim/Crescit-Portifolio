package com.acessorinvestimentos.portfolio.service;

import com.acessorinvestimentos.portfolio.dto.PortfolioResponse;
import com.acessorinvestimentos.portfolio.exception.ResourceNotFoundException;
import com.acessorinvestimentos.portfolio.model.Investment;
import com.acessorinvestimentos.portfolio.model.Portfolio;
import com.acessorinvestimentos.portfolio.repository.InvestmentRepository;
import com.acessorinvestimentos.portfolio.repository.PortfolioRepository;
import java.math.BigDecimal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final InvestmentRepository investmentRepository;

    public PortfolioService(PortfolioRepository portfolioRepository, InvestmentRepository investmentRepository) {
        this.portfolioRepository = portfolioRepository;
        this.investmentRepository = investmentRepository;
    }

    @Transactional
    public Portfolio getOrCreatePortfolio(String userId) {
        return portfolioRepository.findByUserId(userId).orElseGet(() -> createPortfolio(userId));
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioByUserId(String userId) {
        return portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found for user " + userId));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalInvested(String userId) {
        Portfolio portfolio = getPortfolioByUserId(userId);
        return investmentRepository.findAllByPortfolioIdOrderByTicker(portfolio.getId()).stream()
                .map(Investment::getTotalInvested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioResponse(String userId) {
        Portfolio portfolio = getPortfolioByUserId(userId);
        BigDecimal total = investmentRepository.findAllByPortfolioIdOrderByTicker(portfolio.getId()).stream()
                .map(Investment::getTotalInvested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PortfolioResponse(portfolio.getId(), portfolio.getUserId(), total,
                portfolio.getCreatedAt(), portfolio.getUpdatedAt());
    }

    private Portfolio createPortfolio(String userId) {
        try {
            return portfolioRepository.saveAndFlush(new Portfolio(userId));
        } catch (DataIntegrityViolationException exception) {
            return portfolioRepository.findByUserId(userId).orElseThrow(() -> exception);
        }
    }
}
