# Acessor de Investimentos — Serviço de Portfólio

Microsserviço responsável por registrar manualmente os investimentos e aportes de cada usuário. Ele não executa compras ou vendas reais.

## Regras implementadas

- Cada usuário, identificado pelo `userId` do Auth0, possui no máximo uma carteira.
- Cada ticker aparece no máximo uma vez por carteira.
- Um investimento nasce com quantidade, total investido e preço médio iguais a zero.
- Cada aporte é armazenado separadamente e seu valor é calculado por `quantity × unitPrice`.
- Ao adicionar ou excluir um aporte, quantidade total, valor total e preço médio são recalculados pelo histórico restante.
- Um usuário não pode acessar investimentos ou aportes pertencentes a outro usuário.

## Tecnologias

- Java 17 e Spring Boot 3.3
- Spring Web, Validation e Data JPA
- PostgreSQL e Flyway
- JUnit 5, Mockito, MockMvc, H2 e JaCoCo

## Executando com Docker

Copie `.env.example` para `.env` se quiser alterar as credenciais e execute:

```bash
docker compose up --build
```
A API ficará em `http://localhost:8080` e o PostgreSQL em `localhost:5432`.

## Executando localmente

Com PostgreSQL disponível, configure as variáveis de `.env.example` e execute:

```bash
mvn spring-boot:run
```

Para rodar os testes:

```bash
mvn test
```

Os testes usam H2 em memória e não dependem dos containers.
O build falha automaticamente se a cobertura de instruções do JaCoCo ficar abaixo de 80%.

## Deploy automático

O workflow `.github/workflows/deploy.yml` executa os testes, publica a imagem do serviço no Docker Hub e atualiza a aplicação no servidor a cada push na branch `main`.

Configure estes secrets no repositório do GitHub antes do primeiro deploy:

- `DOCKERHUB_TOKEN`: token da conta Docker Hub usada pelo workflow;
- `HOST_TEST`: endereço IP ou domínio do servidor;
- `KEY_TEST`: chave SSH privada do usuário `ubuntu`;
- `DATABASE_PASSWORD`: senha do PostgreSQL exclusivo do serviço de Portfólio.

No servidor, o workflow mantém dois containers separados: `portifolio` para a API e `portfolio-postgres` para o banco. Os dados permanecem no volume `portfolio-postgres-data` entre os deploys.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/portfolios/{userId}` | Busca a carteira e seu total investido |
| `POST` | `/portfolios/{userId}/investments` | Cria um investimento e, se necessário, a carteira |
| `GET` | `/portfolios/{userId}/investments` | Lista os investimentos |
| `GET` | `/portfolios/{userId}/investments/{investmentId}` | Busca um investimento |
| `PUT` | `/portfolios/{userId}/investments/{investmentId}` | Atualiza ticker, nome e tipo |
| `DELETE` | `/portfolios/{userId}/investments/{investmentId}` | Exclui o investimento e seus aportes |
| `POST` | `/portfolios/{userId}/investments/{investmentId}/contributions` | Registra um aporte |
| `GET` | `/portfolios/{userId}/investments/{investmentId}/contributions` | Lista o histórico de aportes |
| `DELETE` | `/portfolios/{userId}/investments/{investmentId}/contributions/{contributionId}` | Exclui um aporte e recalcula o investimento |

### Criar investimento

```json
{
  "ticker": "PETR4",
  "assetName": "Petrobras PN",
  "assetType": "STOCK"
}
```

Tipos aceitos: `STOCK`, `FII`, `ETF` e `FIXED_INCOME`.

### Registrar aporte

```json
{
  "quantity": 10.5,
  "unitPrice": 35.20,
  "contributionDate": "2026-09-28"
}
```
