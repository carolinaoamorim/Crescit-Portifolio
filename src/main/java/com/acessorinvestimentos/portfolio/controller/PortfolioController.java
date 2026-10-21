package com.acessorinvestimentos.portfolio.controller;

import com.acessorinvestimentos.portfolio.dto.ContributionRequest;
import com.acessorinvestimentos.portfolio.dto.InvestmentRequest;
import com.acessorinvestimentos.portfolio.dto.PortfolioResponse;
import com.acessorinvestimentos.portfolio.model.Contribution;
import com.acessorinvestimentos.portfolio.model.Investment;
import com.acessorinvestimentos.portfolio.service.ContributionService;
import com.acessorinvestimentos.portfolio.service.InvestmentService;
import com.acessorinvestimentos.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/portfolios/{userId}")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final InvestmentService investmentService;
    private final ContributionService contributionService;

    public PortfolioController(PortfolioService portfolioService,
                               InvestmentService investmentService,
                               ContributionService contributionService) {
        this.portfolioService = portfolioService;
        this.investmentService = investmentService;
        this.contributionService = contributionService;
    }

    @GetMapping
    @PreAuthorize("#userId == authentication.name and hasAuthority('read:portfolio')")
    public PortfolioResponse getPortfolio(@PathVariable @NotBlank String userId) {
        return portfolioService.getPortfolioResponse(userId);
    }

    @PostMapping("/investments")
    @PreAuthorize("#userId == authentication.name and hasAuthority('write:portfolio')")
    public ResponseEntity<Investment> addInvestment(@PathVariable @NotBlank String userId,
                                                     @Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentService.addInvestment(userId, request));
    }

    @GetMapping("/investments")
    @PreAuthorize("#userId == authentication.name and hasAuthority('read:portfolio')")
    public List<Investment> getInvestments(@PathVariable @NotBlank String userId) {
        return investmentService.getInvestmentsByUserId(userId);
    }

    @GetMapping("/investments/{investmentId}")
    @PreAuthorize("#userId == authentication.name and hasAuthority('read:portfolio')")
    public Investment getInvestment(@PathVariable @NotBlank String userId,
                                    @PathVariable UUID investmentId) {
        return investmentService.getInvestmentById(userId, investmentId);
    }

    @PutMapping("/investments/{investmentId}")
    @PreAuthorize("#userId == authentication.name and hasAuthority('write:portfolio')")
    public Investment updateInvestment(@PathVariable @NotBlank String userId,
                                       @PathVariable UUID investmentId,
                                       @Valid @RequestBody InvestmentRequest request) {
        return investmentService.updateInvestment(userId, investmentId, request);
    }

    @DeleteMapping("/investments/{investmentId}")
    @PreAuthorize("#userId == authentication.name and hasAuthority('write:portfolio')")
    public ResponseEntity<Void> deleteInvestment(@PathVariable @NotBlank String userId,
                                                  @PathVariable UUID investmentId) {
        investmentService.deleteInvestment(userId, investmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/investments/{investmentId}/contributions")
    @PreAuthorize("#userId == authentication.name and hasAuthority('write:portfolio')")
    public ResponseEntity<Contribution> addContribution(@PathVariable @NotBlank String userId,
                                                         @PathVariable UUID investmentId,
                                                         @Valid @RequestBody ContributionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contributionService.addContribution(userId, investmentId, request));
    }

    @GetMapping("/investments/{investmentId}/contributions")
    @PreAuthorize("#userId == authentication.name and hasAuthority('read:portfolio')")
    public List<Contribution> getContributions(@PathVariable @NotBlank String userId,
                                               @PathVariable UUID investmentId) {
        return contributionService.getContributions(userId, investmentId);
    }

    @DeleteMapping("/investments/{investmentId}/contributions/{contributionId}")
    @PreAuthorize("#userId == authentication.name and hasAuthority('write:portfolio')")
    public ResponseEntity<Void> deleteContribution(@PathVariable @NotBlank String userId,
                                                    @PathVariable UUID investmentId,
                                                    @PathVariable UUID contributionId) {
        contributionService.deleteContribution(userId, investmentId, contributionId);
        return ResponseEntity.noContent().build();
    }
}
