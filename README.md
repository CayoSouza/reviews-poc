# Reviews Backend

This project provides a backend service for managing and aggregating user reviews for restaurants with the objective to measure database level aggregations (average). The service supports creating reviews, retrieving them, and calculating the average rating for a specific restaurant. The system is built using Kotlin, Spring Boot, PostgreSQL, and MongoDB.

## Branches

[before-improvements](https://github.com/CayoSouza/reviews-poc/tree/save/before-improvements)

## Architecture Diagram

![Architecture Diagram](imgs/architecture.png)


## Features

- **Create Reviews:** Users can create a review for an order.
- **Retrieve Reviews:** Retrieve a review by order ID.
- **Average Rating:** Calculate the average rating for a specific restaurant based on reviews.
- **Transactional:** Reviews are saved in both PostgreSQL and MongoDB within a single transaction.
- **Testcontainers:** Integration tests are performed using Testcontainers for PostgreSQL and MongoDB containers.

## Tech Stack

- **Kotlin**: Programming language used for building the service.
- **Spring Boot**: Framework for building the backend application.
- **PostgreSQL**: Relational database for storing review data.
- **MongoDB**: NoSQL database for performing aggregations (e.g., calculating the average rating).
- **Docker & Docker Compose**: Used to containerize the services for local development and testing.
- **Testcontainers**: Library for providing integration tests using PostgreSQL and MongoDB containers.

## Setup

### Prerequisites

- Docker and Docker Compose installed.
- Java 17 installed.
- PostgreSQL and MongoDB Docker images available.

### Running the Application

```bash
# Clone the repository
git clone https://github.com/your-username/ifood-reviews.git
cd ifood-reviews

# Build the project
./gradlew build

# Start the services with Docker Compose
docker-compose up -d

# Run the application
./gradlew bootRun
```

### Curls

```text
Create a New Review:
curl -X POST http://localhost:8080/api/reviews \
-H "Content-Type: application/json" \
-d '{
  "orderId": "example-order-id",
  "userId": "example-user-id",
  "restaurantId": "example-restaurant-id",
  "stars": 5,
  "comment": "This is a review comment."
}'

Get a Review by Order ID:
curl -X GET http://localhost:8080/api/reviews/order/{orderId}

Get Average Stars for a Restaurant by Restaurant ID (MongoDB):
curl -X GET "http://localhost:8080/api/reviews/restaurant/{restaurantId}/average?useNoSql=true"

Get Average Stars for a Restaurant by Restaurant ID (PostgreSQL):
curl -X GET "http://localhost:8080/api/reviews/restaurant/{restaurantId}/average?useNoSql=false"

Get Paginated Reviews for a Restaurant by Restaurant ID:
curl -X GET "http://localhost:8080/api/reviews/restaurant/{restaurantId}?page=0&size=10"

Count Reviews by Restaurant ID (MongoDB):
curl -X GET "http://localhost:8080/api/reviews/restaurant/{restaurantId}/count?useNoSql=true"

Count Reviews by Restaurant ID (PostgreSQL):
curl -X GET "http://localhost:8080/api/reviews/restaurant/{restaurantId}/count?useNoSql=false"
```

### MEDIÇÕES DO SLA

[150VUs - create_review - success](k6/150VUs/createReview-summary-e3b0c442-98fc-1fc1-9fd3-256e9df06d05.html)
[200VUs - create_review - failed](k6/failed/createReview-200VUs-summary-e3b0c442-98fc-1fc1-9fd3-256e9df06d05.html)
```text
Tipo de operações: inserção
Arquivos envolvidos (lista de Arquivos c/ os links contidos no repositório que estejam envolvidos na implementação do serviço 1)
Data da medição: 28/08/2024
Descrição das configurações (máquinas/containers utilizadas para o sistema funcionar, ...)
image: mongo:6.0
image: postgres:16.2
Core i5-13600KF
Memory Limit: 15.54GiB
RTX 4070 Ti
Potenciais gargalos do sistema: mais de 200 usuarios em concorrencia por limite de CPU (atingiu 100%, scaling horizontal e/ou vertical necessario)
```

[50-100-200VUs - get_review](k6/200VUs/getReview-summary2-e3b0c442-98fc-1fc1-9fd3-256e9df06d05.html)
```text
Tipo de operações: leitura
Arquivos envolvidos (lista de Arquivos c/ os links contidos no repositório que estejam envolvidos na implementação do serviço 2)
Data da medição: 28/08/2024
Descrição das configurações (máquinas/containers utilizadas para o sistema funcionar, ...)
image: mongo:6.0
image: postgres:16.2
Core i5-13600KF
Memory Limit: 15.54GiB
RTX 4070 Ti
Testes de carga (SLA): latência, vazão e concorrência (limite de requisições simultâneas)
Potenciais gargalos do sistema: mais de 200 usuarios em concorrencia por limite de CPU (atingiu 100%, scaling horizontal e/ou vertical necessario)
```


## Extra Performance Measurements (after restaurant indexes + 10M+ rows in the database)
### Improvements: 
- restaurantId index in both databases; 
- connection pool increased to 200;
- MongoDB using database aggregation instead of memory calculation.
### Data changes:
- +10M reviews (which makes searching by restaurantId more expensive, hence the need of the index);

### 1M reviews latency

MongoDB (with restaurantId index)
```text
count: 200 ms
average: 700 ms
```

Postgres (with restaurantId index)
```text
count: 45 ms
average: 48 ms
```

### 10M reviews latency

MongoDB (with restaurantId index)
```text
count: 1950 ms
average: 7000 ms
```

Postgres (with restaurantId index)
```text
count: 300 ms
average: 350 ms
```

## Possible Improvements

- **Caching:** Implement caching mechanisms to reduce the number of requests to the database.
- **Message Queues:** Use message queues to make review creation faster, more scalable and resilient.
- **Horizontal/Vertical Scaling** Increase the number of instances or resources to handle more requests.
- **Increase connection pool:** Increase the connection pool size to handle more concurrent requests.
- **More Indexes in the Database:** Add more indexes to improve queries performance.