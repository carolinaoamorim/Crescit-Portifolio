-- Dados opcionais para organizar a carteira: onde o investimento está e,
-- para renda fixa, a rentabilidade contratada e o vencimento.
-- Todas as colunas aceitam NULL, então os investimentos existentes não mudam.
ALTER TABLE investments ADD COLUMN institution VARCHAR(60);
ALTER TABLE investments ADD COLUMN rate_type VARCHAR(20);
ALTER TABLE investments ADD COLUMN rate_value NUMERIC(9, 4);
ALTER TABLE investments ADD COLUMN maturity_date DATE;
