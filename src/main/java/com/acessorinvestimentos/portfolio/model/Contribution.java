package com.acessorinvestimentos.portfolio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contributions")
public class Contribution {

    @Id
    private UUID id;

    @Column(name = "investment_id", nullable = false)
    private UUID investmentId;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 8)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "contribution_date", nullable = false)
    private LocalDate contributionDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Contribution() {
    }

    public Contribution(UUID investmentId, BigDecimal quantity, BigDecimal unitPrice, LocalDate contributionDate) {
        this.id = UUID.randomUUID();
        this.investmentId = investmentId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = quantity.multiply(unitPrice).setScale(2, java.math.RoundingMode.HALF_UP);
        this.contributionDate = contributionDate;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getInvestmentId() { return investmentId; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getContributionDate() { return contributionDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
