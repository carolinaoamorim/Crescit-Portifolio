package com.acessorinvestimentos.portfolio.repository;

import com.acessorinvestimentos.portfolio.model.Portfolio;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    Optional<Portfolio> findByUserId(String userId);
}
