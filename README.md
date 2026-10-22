# Advent of Spring

This project is a Spring Boot API with PostgreSQL, Redis cache, and a local RAG experiment using Spring AI + Groq.

## Prerequisites

- Java 25 or Java 23 compatible with the project
- Maven or Maven Wrapper
- Docker Desktop or Docker Engine
- A Groq API key

## 1) Start required Docker services

### PostgreSQL

```bash
docker run \
  --name customer-api-db \
  -e POSTGRES_USER=customer \
  -e POSTGRES_PASSWORD=customer \
  -e POSTGRES_DB=customer \
  -p 5433:5432 \
  -d postgres:17
```

To restart it later:

```bash
docker start customer-api-db
```

To remove it:

```bash
docker rm -f customer-api-db
```

### Redis

```bash
docker run --name my-redis -d -p 6379:6379 redis
```

If you want to remove it:

```bash
docker rm -f my-redis
```

## 2) Export environment variables

The app expects a Groq key for Spring AI.

```bash
export GROQ_API_KEY=your_real_groq_api_key_here
```

You can also set it permanently in your shell profile:

```bash
echo 'export GROQ_API_KEY=your_real_groq_api_key_here' >> ~/.zshrc
source ~/.zshrc
```

If you use bash instead of zsh:

```bash
echo 'export GROQ_API_KEY=your_real_groq_api_key_here' >> ~/.bashrc
source ~/.bashrc
```

## 3) Build the project

```bash
./mvnw clean package
```

Or if you use Maven directly:

```bash
mvn clean package
```

## 4) Run the app locally

```bash
GROQ_API_KEY=your_real_groq_api_key_here ./mvnw spring-boot:run
```

Or set the variable first, then run:

```bash
export GROQ_API_KEY=your_real_groq_api_key_here
./mvnw spring-boot:run
```

The app will start on:

```text
http://localhost:8181
```

## 5) RAG configuration

The app uses Spring AI with Groq and a local SimpleVectorStore reading `src/main/resources/docs/qa.txt`.

The relevant configuration is in `src/main/resources/application.yml`:

```yml
app:
  rag:
    enabled: true

spring:
  ai:
    openai:
      base-url: https://api.groq.com/openai
      api-key: ${GROQ_API_KEY}
      chat:
        options:
          model: llama-3.1-8b-instant
      embedding:
        options:
          model: text-embedding-3-small
```

Important:
- `base-url` must be `https://api.groq.com/openai`
- do not add `/v1` by hand
- Spring AI appends `/v1` automatically
- the Groq key must be a real key, not a placeholder

## 6) Run only the RAG demo

The app includes a CommandLineRunner gated by:

```yml
app:
  rag:
    enabled: true
```

If you want to run with that flag explicitly:

```bash
GROQ_API_KEY=your_real_groq_api_key_here ./mvnw spring-boot:run -Dspring-boot.run.arguments='--app.rag.enabled=true'
```

## 7) Useful commands

### Verify build

```bash
./mvnw verify
```

### Build Docker image

```bash
docker build -t adventofspring:v1 .
```

### Run Docker container

```bash
docker run -p 8181:8181 adventofspring:v1
```

### Docker Compose

```bash
docker compose up
docker compose down
```

### Actuator logs debug check

```bash
curl -X POST "http://localhost:8181/actuator/loggers/com.caique.adventOfSpring" \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

```bash
curl http://localhost:8181/actuator/loggers/com.caique.adventOfSpring | jq
```

## Troubleshooting

### 404 on embeddings or `/v1/v1/embeddings`

This usually means the base URL is wrong.

Use:

```yml
base-url: https://api.groq.com/openai
```

Not:

```yml
base-url: https://api.groq.com/openai/v1
```

### App fails to start because of missing key

Make sure you exported the variable before running:

```bash
export GROQ_API_KEY=your_real_groq_api_key_here
```

### Database connection fails

Ensure PostgreSQL is running on port 5433 and the credentials match:

```text
jdbc:postgresql://localhost:5433/customer
username: customer
password: customer
```

### Redis not available

Make sure Redis is running on:

```text
localhost:6379
```
