package com.acessorinvestimentos.portfolio.repository;

import com.acessorinvestimentos.portfolio.model.Contribution;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributionRepository extends JpaRepository<Contribution, UUID> {
    List<Contribution> findAllByInvestmentIdOrderByContributionDateDescCreatedAtDesc(UUID investmentId);
    Optional<Contribution> findByIdAndInvestmentId(UUID id, UUID investmentId);
    void deleteAllByInvestmentId(UUID investmentId);
}
