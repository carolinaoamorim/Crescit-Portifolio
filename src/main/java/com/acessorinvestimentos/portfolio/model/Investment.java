package com.acessorinvestimentos.portfolio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "investments", uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "ticker"}))
public class Investment {

    @Id
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(name = "total_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal totalQuantity;

    @Column(name = "total_invested", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalInvested;

    @Column(name = "average_price", nullable = false, precision = 19, scale = 8)
    private BigDecimal averagePrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private long version;

    protected Investment() {
    }

    public Investment(UUID portfolioId, String ticker, String assetName, AssetType assetType) {
        this.id = UUID.randomUUID();
        this.portfolioId = portfolioId;
        this.ticker = ticker.trim().toUpperCase();
        this.assetName = assetName.trim();
        this.assetType = assetType;
        this.totalQuantity = BigDecimal.ZERO;
        this.totalInvested = BigDecimal.ZERO;
        this.averagePrice = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getPortfolioId() { return portfolioId; }
    public String getTicker() { return ticker; }
    public String getAssetName() { return assetName; }
    public AssetType getAssetType() { return assetType; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public BigDecimal getTotalInvested() { return totalInvested; }
    public BigDecimal getAveragePrice() { return averagePrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void updateDetails(String ticker, String assetName, AssetType assetType) {
        this.ticker = ticker.trim().toUpperCase();
        this.assetName = assetName.trim();
        this.assetType = assetType;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTotals(BigDecimal quantity, BigDecimal invested, BigDecimal averagePrice) {
        this.totalQuantity = quantity;
        this.totalInvested = invested;
        this.averagePrice = averagePrice;
        this.updatedAt = LocalDateTime.now();
    }
}
