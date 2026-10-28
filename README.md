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
- Spring Web, Security, OAuth2 Resource Server, Validation e Data JPA
- PostgreSQL e Flyway
- JUnit 5, Mockito, MockMvc, H2 e JaCoCo

## Autenticação e autorização

A API valida access tokens JWT emitidos pelo Auth0. Toda requisição deve enviar:

```http
Authorization: Bearer ACCESS_TOKEN
```

O token deve ter o issuer e o audience configurados e conter uma das permissões:

- `read:portfolio` para operações `GET`;
- `write:portfolio` para operações `POST`, `PUT` e `DELETE`.

Além da permissão, o claim `sub` do token precisa ser igual ao `userId` da rota. Assim, um usuário autenticado não consegue acessar a carteira de outra pessoa.

Configure as seguintes variáveis:

```env
AUTH0_ISSUER=https://SEU_DOMINIO_AUTH0/
AUTH0_AUDIENCE=https://api.crescit-investimentos
FRONTEND_URL=https://crescit-frontend.vercel.app
```

Para desenvolvimento local, use `FRONTEND_URL=http://localhost:5173`.

O serviço valida assinatura, expiração, issuer e audience do token. Requisições sem token ou com token inválido retornam `401`; tokens válidos sem permissão ou de outro usuário retornam `403`.

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
- `KEY_TEST`: chave SSH privada do usuário `ubuntu`.

Configure também estas variáveis em **Settings → Secrets and variables → Actions → Variables**:

- `AUTH0_ISSUER`: issuer completo do tenant Auth0, com `/` no final;
- `AUTH0_AUDIENCE`: identifier da API no Auth0;
- `FRONTEND_URL`: origem permitida para o frontend publicado.

No servidor, o workflow mantém dois containers separados: `portifolio` para a API e `portfolio-postgres` para o banco. Os dados permanecem no volume `portfolio-postgres-data` entre os deploys. O banco usa as credenciais locais `portfolio/portfolio` e não publica a porta `5432` fora da rede Docker.

## Endpoints

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| `GET` | `/portfolios/{userId}` | `read:portfolio` | Busca a carteira e seu total investido |
| `POST` | `/portfolios/{userId}/investments` | `write:portfolio` | Cria um investimento e, se necessário, a carteira |
| `GET` | `/portfolios/{userId}/investments` | `read:portfolio` | Lista os investimentos |
| `GET` | `/portfolios/{userId}/investments/{investmentId}` | `read:portfolio` | Busca um investimento |
| `PUT` | `/portfolios/{userId}/investments/{investmentId}` | `write:portfolio` | Atualiza ticker, nome e tipo |
| `DELETE` | `/portfolios/{userId}/investments/{investmentId}` | `write:portfolio` | Exclui o investimento e seus aportes |
| `POST` | `/portfolios/{userId}/investments/{investmentId}/contributions` | `write:portfolio` | Registra um aporte |
| `GET` | `/portfolios/{userId}/investments/{investmentId}/contributions` | `read:portfolio` | Lista o histórico de aportes |
| `DELETE` | `/portfolios/{userId}/investments/{investmentId}/contributions/{contributionId}` | `write:portfolio` | Exclui um aporte e recalcula o investimento |

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
