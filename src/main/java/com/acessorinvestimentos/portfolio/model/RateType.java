package com.acessorinvestimentos.portfolio.model;

/** Como a rentabilidade de um investimento de renda fixa é contratada. */
public enum RateType {
    /** Percentual do CDI, por exemplo 110 (% do CDI) */
    CDI_PERCENT,
    /** Taxa prefixada ao ano, por exemplo 12,5 (% a.a.) */
    PREFIXED,
    /** IPCA mais uma taxa ao ano, por exemplo 6 (IPCA + 6% a.a.) */
    IPCA_PLUS
}
