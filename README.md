# Triagem UPA — API

Backend do sistema de triagem hospitalar baseado no **Protocolo de Manchester**.
Projeto acadêmico de Estruturas de Dados e Análise de Algoritmos (UnP).

## Stack

- Java 21, Spring Boot 3.3
- PostgreSQL 16 (produção) / H2 (perfil `dev`)
- JPA/Hibernate, Spring Validation, Lombok
- Maven

## Algoritmo — Priority Queue (Max-Heap por urgência)

A fila de espera é uma **Priority Queue** ordenada por:

1. `priority ASC` — P1 (Emergência) > P2 (Muito Urgente) > P3 > P4 > P5.
2. `arrivalTime ASC` — FIFO como critério de desempate dentro do mesmo nível.

A ordenação é materializada pela query JPQL em
[`PatientRepository.findQueueOrdered`](src/main/java/br/edu/unp/triagem/api/repository/PatientRepository.java).
A demonstração canônica: cadastrar vários P4/P5 e depois um P1 — o P1 vai
direto para o topo, comportamento impossível numa fila FIFO pura.

Teste de invariante: [`PriorityQueueOrderingTest`](src/test/java/br/edu/unp/triagem/api/PriorityQueueOrderingTest.java).

## Endpoints

| Método | Path                         | Descrição                                                      |
|--------|------------------------------|----------------------------------------------------------------|
| POST   | `/api/patients`              | Cadastra paciente (gera ticket `T001`, `T002`... automaticamente) |
| GET    | `/api/patients/queue`        | Fila ordenada: IN_SERVICE + WAITING por prioridade             |
| POST   | `/api/patients/next`         | Encerra IN_SERVICE atual e promove o head da priority queue    |
| POST   | `/api/patients/{id}/finish`  | Encerra atendimento sem chamar o próximo                       |
| GET    | `/api/patients/history`      | Pacientes ATTENDED, ordem `attendedAt DESC`                    |

### Contratos

```jsonc
// POST /api/patients
{ "name": "João Pedro", "priority": 1 }

// PatientResponse
{
  "id": 1, "name": "João Pedro", "ticket": "T001",
  "priority": 1, "arrivalTime": "08:25",
  "status": "WAITING", "attendedAt": null
}
```

## Como rodar

### Dev local com H2 (sem Docker)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Dev local com PostgreSQL via Docker

```bash
docker compose up --build
```

A API sobe em `http://localhost:8080`.

### Testes

```bash
mvn test
```

## Variáveis de ambiente

| Variável         | Default                                      | Descrição                       |
|------------------|----------------------------------------------|---------------------------------|
| `DB_HOST`        | `localhost`                                  | Host do PostgreSQL              |
| `DB_NAME`        | `triagem`                                    | Nome do banco                   |
| `DB_USER`        | `postgres`                                   | Usuário                         |
| `DB_PASS`        | `postgres`                                   | Senha                           |
| `CORS_ORIGINS`   | `http://localhost:5173,...`                  | Origens CORS (vírgula-separado) |

## Deploy

Via GitHub Actions → GHCR → SSH no Droplet DigitalOcean
(`.github/workflows/deploy.yml`). Secrets necessários:

- `DROPLET_HOST`, `DROPLET_USER`, `DROPLET_SSH_KEY`
- `DB_USER`, `DB_PASS`

O Nginx do Droplet faz proxy de `/api/` → `localhost:8080` em
`triagem.gilbertopaiva.me`.
