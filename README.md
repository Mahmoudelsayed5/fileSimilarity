# File similarity checker

A Spring Boot application that compares a reference file (`fileA.txt`) against a pool of `.txt` files to compute similarity scores based on shared words.

# Features

- Compute similarity using word matching
- Supports both "sequential" and "parallel" processing
- Compare performance of both approaches
- RESTful endpoints
- Unit tested with JUnit 5

# Endpoints

Description                         
GET  =>   `/similarity`         =>   Get similarity scores                
GET  =>   `/similarity/benchmark` => Compare sequential vs parallel speed 

# Technologies

- Java 21
- Spring Boot
- Maven
- JUnit 5
- GitHub Actions (CI)


# Run the Project

```bash
mvn spring-boot:run

# Output

### 1. `/similarity` Endpoint Response
![Similarity Result](output/similarity-result.png)

### 2. `/similarity/benchmark` Endpoint Response
![Benchmark Result](output/benchmark-result.png)