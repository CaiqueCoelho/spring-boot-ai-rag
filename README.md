# Advent of Spring

This project is a Spring Boot API with PostgreSQL, Redis cache, and a local RAG experiment using Spring AI + Ollama.

## What changed and what we learned

This project started as an experiment with a paid/OpenAI-style setup, but the stable and free path is to use Ollama locally. That keeps the app running without API costs and is better for demos, prototypes, and local development.

Important lessons from the setup:

- `SimpleVectorStore` is useful for small local RAG experiments, but it is not a production-grade vector database.
- For a production RAG backend, a real vector database with indexing, filtering, scaling, and operational support is usually the better choice.
- Retrieval quality improves with `top-k`, metadata filters, and a confidence threshold before sending results to the model.
- When multiple embedding beans are available, Spring can fail with a bean ambiguity error unless you use `@Qualifier` on the provider you want.
- A stale Java process can keep port `8181` occupied, which blocks startup even when the app itself is fine. A plain `kill` may not be enough; `kill -9` forces it.
- With `spring-boot:run`, `-Dserver.port=...` is ignored because the app runs in a forked JVM. Use `-Dspring-boot.run.arguments=--server.port=...` instead.
- The knowledge base in this project is located in `src/main/resources/docs/qa.txt`, and the vector store persists to `src/main/resources/data/vectorestore.json`.

The current implementation uses a local Ollama chat model and embedding model, with the retrieval logic wrapped in a dedicated RAG service that tries top-k retrieval and applies a simple confidence gate before answering.

## Prerequisites

- Java 25 or Java 23 compatible with the project
- Maven or Maven Wrapper
- Docker Desktop or Docker Engine
- Ollama installed locally

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

## 2) Install and run Ollama locally

Install Ollama from:

https://ollama.com/download

After installation, pull the models used by the app:

```bash
ollama pull llama3.1
ollama pull nomic-embed-text
```

You can confirm models are available:

```bash
ollama list
```

Then start the Ollama server:

```bash
ollama serve
```

The app expects Ollama to be available at:

```text
http://localhost:11434
```

If the server is not responding, test it with:

```bash
curl http://localhost:11434/api/tags
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
./mvnw spring-boot:run
```

The app will start on:

```text
http://localhost:8181
```

## 5) RAG configuration

The app uses Spring AI with Ollama and a local `SimpleVectorStore` reading `src/main/resources/docs/qa.txt`.

The relevant configuration is in `src/main/resources/application.yml`:

```yml
app:
  rag:
    enabled: true

spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.1
      embedding:
        options:
          model: nomic-embed-text
```

This uses local models and avoids any paid API key.

A simple local RAG setup like this is fine for experiments, prototypes, or small internal tools. For production, you would normally move to a proper vector database and add better retrieval controls, indexing, chunking, monitoring, and observability.

## 6) Run only the RAG demo

The app includes a CommandLineRunner gated by:

```yml
app:
  rag:
    enabled: true
```

If you want to run with that flag explicitly:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments='--app.rag.enabled=true'
```

## 7) Useful commands

### Verify build

```bash
./mvnw verify
```

### Run with a different port

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8182
```

A plain `-Dserver.port=8182` does not work here: it sets the property on the Maven JVM, not on the forked app JVM, so the app still uses `server.port` from `application.yml`. Passing it as a JVM argument also works:

```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8182"
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

### App starts but RAG fails

Check that Ollama is running locally:

```bash
curl http://localhost:11434/api/tags
```

If the models are missing, pull them again:

```bash
ollama pull llama3.1
ollama pull nomic-embed-text
```

### App fails to start on port 8181

A previous Java process may still be running. Check and stop it:

```bash
lsof -nP -iTCP:8181 -sTCP:LISTEN
kill <PID>
```

If the same PID is still listening after `kill`, the JVM ignored SIGTERM or hung on shutdown. Check what it is and force it:

```bash
ps -o pid,ppid,lstart,command -p <PID>
kill -9 <PID>
```

If it was started from IntelliJ, stop it from the Run/Debug window instead so the IDE does not keep it alive.

If you want to confirm whether the port is free before starting the app:

```bash
lsof -nP -iTCP:8181 -sTCP:LISTEN || echo "Port 8181 is free"
```

### Bean ambiguity error with embedding models

If Spring logs something like `required a single bean, but 2 were found`, you probably still have both OpenAI and Ollama embedding/chat beans on the classpath. In this project, the fix was to let the vector store explicitly use the Ollama embedding bean via `@Qualifier("ollamaEmbeddingModel")`.

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

## Production RAG guidance

For a real production backend, a local `SimpleVectorStore` is a good learning tool but not the final architecture for scale. A production setup usually includes:

- a dedicated vector database such as PGVector, Pinecone, Weaviate, Qdrant, or Milvus
- chunking strategy and document metadata
- top-k search with filters
- confidence checks and fallback logic
- monitoring, observability, and retraining loops
- separate indexing pipelines for document updates

The simple pattern used here is a solid starting point for a demo or an internal tool, but a production RAG service should be designed around data freshness, performance, and governance.
