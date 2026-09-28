CREATE TABLE portfolios (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE investments (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    ticker VARCHAR(20) NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    asset_type VARCHAR(30) NOT NULL,
    total_quantity NUMERIC(19, 8) NOT NULL DEFAULT 0,
    total_invested NUMERIC(19, 2) NOT NULL DEFAULT 0,
    average_price NUMERIC(19, 8) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_investment_portfolio_ticker UNIQUE (portfolio_id, ticker)
);

CREATE TABLE contributions (
    id UUID PRIMARY KEY,
    investment_id UUID NOT NULL REFERENCES investments(id) ON DELETE CASCADE,
    quantity NUMERIC(19, 8) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 8) NOT NULL CHECK (unit_price > 0),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    contribution_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_investments_portfolio_id ON investments(portfolio_id);
CREATE INDEX idx_contributions_investment_id ON contributions(investment_id);
