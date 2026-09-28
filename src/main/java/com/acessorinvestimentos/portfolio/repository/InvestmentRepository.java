package com.acessorinvestimentos.portfolio.repository;

import com.acessorinvestimentos.portfolio.model.Investment;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
    List<Investment> findAllByPortfolioIdOrderByTicker(UUID portfolioId);
    Optional<Investment> findByIdAndPortfolioId(UUID id, UUID portfolioId);
    boolean existsByPortfolioIdAndTickerIgnoreCase(UUID portfolioId, String ticker);
    boolean existsByPortfolioIdAndTickerIgnoreCaseAndIdNot(UUID portfolioId, String ticker, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Investment> findWithLockById(UUID id);
}
