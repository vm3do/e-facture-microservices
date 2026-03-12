# Docker Guide for e-facture-ms — Complete Beginner Tutorial

> This guide assumes you have **zero Docker knowledge**. It will teach you every concept,
> every command, and walk you through creating all the files yourself.
> **Read it top to bottom. Do NOT skip sections.**

---

## Table of Contents

1. [What is Docker and Why Do We Need It?](#1-what-is-docker-and-why-do-we-need-it)
2. [Key Vocabulary (10 words you MUST know)](#2-key-vocabulary)
3. [Dockerfile vs docker-compose.yml — The Big Difference](#3-dockerfile-vs-docker-composeyml)
4. [What is a Multi-Stage Dockerfile?](#4-what-is-a-multi-stage-dockerfile)
5. [Step-by-Step: Your First Dockerfile (config-service)](#5-step-by-step-your-first-dockerfile)
6. [Building and Running Your First Container](#6-building-and-running-your-first-container)
7. [Creating Dockerfiles for the Other 4 Services](#7-creating-dockerfiles-for-the-other-4-services)
8. [What is docker-compose and Why Do We Need It?](#8-what-is-docker-compose-and-why-do-we-need-it)
9. [Step-by-Step: Writing docker-compose.yml](#9-step-by-step-writing-docker-composeyml)
10. [Adding MySQL with Persistent Volumes](#10-adding-mysql-with-persistent-volumes)
11. [Adding Prometheus (Monitoring)](#11-adding-prometheus)
12. [Adding Grafana (Dashboards)](#12-adding-grafana)
13. [Networks Explained: ms-network](#13-networks-explained)
14. [The Final docker-compose.yml (Full Picture)](#14-the-final-docker-composeyml)
15. [Essential Docker Commands Cheatsheet](#15-essential-docker-commands-cheatsheet)
16. [Troubleshooting Common Errors](#16-troubleshooting-common-errors)

---

## 1. What is Docker and Why Do We Need It?

### The Problem Without Docker

Right now, to run your e-facture project, every developer must:
- Install Java 17 on their machine
- Install Maven
- Install MySQL
- Configure the right ports
- Hope that their OS doesn't cause weird issues

If one teammate uses Windows and another uses Mac, things break.
If someone has Java 21 instead of Java 17, things break.

### The Solution With Docker

Docker lets you package your application **and everything it needs** (Java, Maven, configs)
into a **container**. A container is like a tiny, lightweight virtual machine that runs
the same way on every computer.

Think of it like this:
- **Without Docker**: "It works on my machine" 🤷
- **With Docker**: "It works on EVERY machine" ✅

---

## 2. Key Vocabulary

Before writing a single line, memorize these 10 words:

| Term | What it means | Real-world analogy |
|------|--------------|-------------------|
| **Image** | A read-only template that contains your app + its dependencies | A recipe for a cake |
| **Container** | A running instance of an image | The actual cake baked from the recipe |
| **Dockerfile** | A text file with instructions to BUILD an image | The recipe card itself |
| **docker-compose.yml** | A file that defines and runs MULTIPLE containers together | A menu for an entire dinner |
| **Build** | Creating an image from a Dockerfile | Baking the cake |
| **Volume** | Persistent storage that survives container restarts | A USB drive plugged into the container |
| **Network** | A virtual network that lets containers talk to each other | A private WiFi just for your containers |
| **Port mapping** | Connecting a port on YOUR machine to a port inside the container | A phone extension forwarding to an office |
| **Stage** | One phase in a multi-stage Dockerfile | One step in a recipe (prep, then cook) |
| **Registry** | A place to store/share images (like Docker Hub) | An App Store for Docker images |

---

## 3. Dockerfile vs docker-compose.yml

This is the **#1 confusion** for beginners. Let's clear it up:

### Dockerfile = How to build ONE image

A `Dockerfile` contains instructions to create a single Docker image.
You write ONE `Dockerfile` per service.

```
Dockerfile → builds → Image → runs as → Container
```

**Example**: `client-service/Dockerfile` tells Docker: 
"Take Java 17, copy my code, run Maven to compile, then package the .jar"

### docker-compose.yml = How to run MANY containers together

A `docker-compose.yml` says: "Take all these images, create containers from them,
connect them on a network, and start them together."

```
docker-compose.yml → orchestrates → Container A + Container B + Container C + ...
```

**Example**: Your `docker-compose.yml` will say:
"Start config-service, discovery-service, gateway-service, client-service,
produit-service, MySQL, Prometheus, and Grafana — all connected on ms-network."

### Summary

| | Dockerfile | docker-compose.yml |
|---|---|---|
| **Scope** | ONE service | ALL services together |
| **Purpose** | Build an image | Orchestrate containers |
| **You need** | One per service | One for the whole project |
| **Analogy** | Recipe for one dish | Full dinner menu + cooking schedule |

---

## 4. What is a Multi-Stage Dockerfile?

### The Problem With a Single Stage

If you use ONE stage, your final image contains everything:
Java JDK, Maven, source code, compiled code, test files...
That makes the image **huge** (1+ GB) and **insecure** (source code exposed).

### The Solution: Two Stages

A multi-stage build uses TWO phases:

```
Stage 1 (BUILD)    →    Stage 2 (RUNTIME)
─────────────────        ─────────────────
Has Maven + JDK          Has only JRE (smaller)
Copies source code       Copies ONLY the .jar
Runs `mvn package`       Runs `java -jar app.jar`
~800 MB                  ~200 MB
Thrown away after!        This is your final image!
```

**Key insight**: Stage 1 is temporary. Docker throws it away after extracting the .jar.
Your final image (Stage 2) is small and contains only what's needed to RUN the app.

---

## 5. Step-by-Step: Your First Dockerfile

We'll start with `config-service` because it's the simplest (no database dependency).

### What you need to do:

1. Open your terminal / file explorer
2. Navigate to `e-facture-ms/config-service/`
3. Create a new file called `Dockerfile` (no extension, capital D)
4. Type the following, and I'll explain EVERY line:

### Line-by-line Explanation

```dockerfile
# ========================
# STAGE 1: BUILD
# ========================
FROM maven:3.9.9-eclipse-temurin-17 AS build
```
**What this does**: 
- `FROM` = "Start from this base image". Think of it as choosing your starting ingredient.
- `maven:3.9.9-eclipse-temurin-17` = A pre-made image that already has Maven 3.9.9 and Java 17 (Temurin JDK) installed. You don't need to install them yourself!
- `AS build` = Give this stage a name ("build") so we can reference it later.

```dockerfile
WORKDIR /app
```
**What this does**:
- `WORKDIR` = "Set the working directory inside the container to `/app`".
- It's like running `cd /app`. If `/app` doesn't exist, Docker creates it.
- Every command after this runs inside `/app`.

```dockerfile
COPY pom.xml .
```
**What this does**:
- `COPY` = Copy a file from YOUR computer into the container.
- `pom.xml` = The file on your machine (relative to the Dockerfile location, so `config-service/pom.xml`).
- `.` = The destination inside the container (which is `/app` because of WORKDIR).
- **Why copy pom.xml first?** Docker caching! If `pom.xml` hasn't changed, Docker won't re-download dependencies. This saves MINUTES on rebuilds.

```dockerfile
RUN mvn dependency:go-offline -B
```
**What this does**:
- `RUN` = Execute a command inside the container during the BUILD process.
- `mvn dependency:go-offline` = Download all Maven dependencies defined in pom.xml.
- `-B` = "Batch mode" — no interactive prompts, cleaner output.
- **Why a separate step?** Docker caching again. Dependencies are downloaded once and cached. Only re-downloaded if pom.xml changes.

```dockerfile
COPY src ./src
```
**What this does**:
- Copy your entire `src/` folder from your machine into `/app/src` in the container.
- We do this AFTER downloading dependencies so that code changes don't trigger a re-download.

```dockerfile
RUN mvn package -DskipTests -B
```
**What this does**:
- Compile your Java code and package it into a `.jar` file.
- `-DskipTests` = Skip running tests (we want a fast build; tests should run in CI/CD).
- The `.jar` file will be created in `/app/target/` inside the container.

```dockerfile
# ========================
# STAGE 2: RUNTIME
# ========================
FROM eclipse-temurin:17-jre-jammy
```
**What this does**:
- Start a **NEW, fresh image** — this is Stage 2.
- `eclipse-temurin:17-jre-jammy` = Has ONLY the Java 17 **JRE** (Java Runtime Environment).
- JRE vs JDK: JDK is for compiling code (bigger), JRE is for running code (smaller). Since we already compiled in Stage 1, we only need JRE now.
- `jammy` = Based on Ubuntu 22.04 (lightweight).

```dockerfile
WORKDIR /app
```
**What this does**: Same as before — set working directory to `/app` in this new stage.

```dockerfile
COPY --from=build /app/target/*.jar app.jar
```
**What this does**:
- `COPY --from=build` = Copy a file **from Stage 1** (named "build") into Stage 2.
- `/app/target/*.jar` = The compiled .jar file from Stage 1.
- `app.jar` = Rename it to `app.jar` in the current directory (`/app/app.jar`).
- **This is the magic of multi-stage builds!** We only take the .jar and leave everything else behind (Maven, source code, etc.).

```dockerfile
EXPOSE 9999
```
**What this does**:
- `EXPOSE` = Document which port the container listens on.
- `9999` because config-service runs on port 9999 (from your `application.properties`).
- **Important**: `EXPOSE` does NOT actually open the port! It's just documentation. The real port opening happens in `docker-compose.yml` or with `docker run -p`.

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```
**What this does**:
- `ENTRYPOINT` = The command that runs when the container starts.
- `["java", "-jar", "app.jar"]` = Run the Spring Boot application.
- Uses "exec form" (JSON array) instead of "shell form" (`java -jar app.jar`). Exec form is preferred because the Java process becomes PID 1 and receives shutdown signals correctly.

### The Complete Dockerfile for config-service

Create the file `config-service/Dockerfile` with this content:

```dockerfile
# STAGE 1: BUILD — compile the application using Maven
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# STAGE 2: RUNTIME — run the compiled jar with minimal image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java", "-jar", "app.jar"]
```

That's **13 lines**. Every production Dockerfile for your services will follow this exact pattern.
The only things that change between services are:
- The `EXPOSE` port number
- Nothing else (they're all Maven + Spring Boot + Java 17)

---

## 6. Building and Running Your First Container

Now that you have a Dockerfile, let's use it!

### Step 1: Open a terminal and navigate to config-service

```bash
cd e-facture-ms/config-service
```

### Step 2: Build the image

```bash
docker build -t efacture-config-service:dev .
```

Let's break down this command:
- `docker build` = "Build an image from a Dockerfile"
- `-t efacture-config-service:dev` = "Tag" (name) the image. Format is `name:version`.
  - `efacture-config-service` = the image name (you choose this)
  - `dev` = the version/tag (you choose this — could be `v1`, `latest`, `dev`, etc.)
- `.` = "The build context is the current directory". Docker will look for a `Dockerfile` here.

**What you'll see**: Docker will download the Maven base image (first time only), then run each
instruction. Each step creates a new "layer". This takes 2-5 minutes the first time.

### Step 3: Verify the image was created

```bash
docker images
```
This lists all images on your machine. You should see `efacture-config-service` with tag `dev`.

### Step 4: Run a container from the image

```bash
docker run --rm -p 9999:9999 efacture-config-service:dev
```

Breaking it down:
- `docker run` = "Create and start a container from an image"
- `--rm` = "Automatically delete the container when it stops" (clean up after yourself)
- `-p 9999:9999` = "Map port 9999 on MY machine to port 9999 in the container"
  - Format: `HOST_PORT:CONTAINER_PORT`
  - Without this, the container runs but you can't access it from your browser!
- `efacture-config-service:dev` = The image to run

**To stop it**: Press `Ctrl+C` in the terminal.

### Step 5: Test it

While the container is running, open a browser and go to: `http://localhost:9999`
If you see a Spring Boot response (even an error page), it's working!

### Common Build Errors and Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `failed to read dockerfile` | Dockerfile not found | Make sure the file is named `Dockerfile` (capital D, no extension) |
| `COPY failed: file not found` | Wrong path in COPY | Make sure `pom.xml` and `src/` exist in the same folder as Dockerfile |
| `BUILD FAILURE` during `mvn package` | Java compilation error | Fix your code first, then rebuild |
| `port is already allocated` | Port 9999 already in use | Stop whatever is using that port, or change the host port: `-p 9998:9999` |

---

## 7. Creating Dockerfiles for the Other 4 Services

Now that you understand every line, create a `Dockerfile` in each service folder.
**The structure is identical** — only the `EXPOSE` port changes.

### What to create:

| Service | Create file at | EXPOSE port | Why this port? |
|---------|---------------|-------------|----------------|
| config-service | `config-service/Dockerfile` | 9999 | Set in `application.properties`: `server.port=9999` |
| discovery-service | `discovery-service/Dockerfile` | 8761 | Set in `application.properties`: `server.port=8761` |
| gateway-service | `gateway-service/Dockerfile` | 8080 | Set in `application.properties`: `server.port=8080` |
| client-service | `client-service/Dockerfile` | 8081 | Set in `application.yml`: `server.port: 8081` |
| produit_service | `produit_service/Dockerfile` | 8082 | Set in `application.yml`: `server.port: 8082` |

### Template (copy this into each Dockerfile, change only EXPOSE):

```dockerfile
# STAGE 1: BUILD
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# STAGE 2: RUNTIME
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE <PORT_NUMBER_HERE>
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Replace `<PORT_NUMBER_HERE>` with the correct port from the table above.

### Verify each one builds

After creating each Dockerfile, test it:

```bash
cd discovery-service
docker build -t efacture-discovery-service:dev .
```

Repeat for each service. Fix any errors before moving on.

---

## 8. What is docker-compose and Why Do We Need It?

### The Problem

You now have 5 Dockerfiles. To run everything, you'd need to type:

```bash
docker run --rm -p 9999:9999 --network ms-network efacture-config-service:dev
docker run --rm -p 8761:8761 --network ms-network efacture-discovery-service:dev
docker run --rm -p 8080:8080 --network ms-network efacture-gateway-service:dev
docker run --rm -p 8081:8081 --network ms-network efacture-client-service:dev
docker run --rm -p 8082:8082 --network ms-network efacture-produit-service:dev
```

Plus MySQL, Prometheus, Grafana... That's **8 terminal windows** and **8 long commands**.
And you'd need to start them in the right ORDER (config first, then discovery, then the rest).

### The Solution: docker-compose

`docker-compose` lets you define ALL of this in ONE file and start everything with ONE command:

```bash
docker-compose up
```

That's it. One command. All 8 containers start, connected, in the right order.

### docker run vs docker-compose

| | `docker run` | `docker-compose up` |
|---|---|---|
| **Starts** | ONE container | ALL containers |
| **Config stored in** | Command-line flags | `docker-compose.yml` file |
| **Networks** | Manual setup | Automatic |
| **Start order** | You manage it | `depends_on` handles it |
| **Good for** | Quick tests | Running the full project |

---

## 9. Step-by-Step: Writing docker-compose.yml

Open the file `e-facture-ms/docker-compose.yml` (at the project root, next to the service folders).

We'll build it piece by piece. **Don't paste the whole thing at once** — understand each section.

### Part 1: The Header

```yaml
services:
```

**What this does**:
- This is the top-level key. Everything under `services:` defines a container.
- (Older guides show `version: "3.8"` at the top. This is **no longer needed** in modern Docker Compose.)

### Part 2: config-service (the first service)

```yaml
services:

  config-service:
    build:
      context: ./config-service
      dockerfile: Dockerfile
    container_name: config-service
    ports:
      - "9999:9999"
    networks:
      - ms-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9999/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
```

Let's break down EVERY line:

- **`config-service:`** — The name of this service. You choose it. Other services will use this name to connect to it (like a hostname).

- **`build:`** — Tells Docker Compose to BUILD an image (instead of pulling a pre-made one).
  - **`context: ./config-service`** — "The build context is the `config-service/` folder". Docker will look for files here when processing `COPY` commands.
  - **`dockerfile: Dockerfile`** — "Use the file named `Dockerfile` in that context folder". This is the default, but it's good to be explicit.

- **`container_name: config-service`** — Give the container a fixed name. Without this, Docker generates a random name like `e-facture-ms-config-service-1`.

- **`ports:`** — Map ports from your machine to the container.
  - **`"9999:9999"`** — `HOST:CONTAINER`. You can access the service at `localhost:9999` on your machine.

- **`networks:`** — Which network(s) this container joins.
  - **`ms-network`** — A custom network we'll define later. All containers on the same network can talk to each other using their service names.

- **`healthcheck:`** — Docker will periodically check if the service is healthy.
  - **`test:`** — The command to run. `curl -f http://localhost:9999/actuator/health` calls the Spring Boot health endpoint.
  - **`interval: 10s`** — Check every 10 seconds.
  - **`timeout: 5s`** — If no response in 5 seconds, it's considered failed.
  - **`retries: 5`** — After 5 failures, mark the container as "unhealthy".

### Part 3: discovery-service

```yaml
  discovery-service:
    build:
      context: ./discovery-service
      dockerfile: Dockerfile
    container_name: discovery-service
    ports:
      - "8761:8761"
    networks:
      - ms-network
    depends_on:
      config-service:
        condition: service_healthy
```

**New concept — `depends_on`**:

- **`depends_on:`** — "Don't start this container until the listed services are ready."
  - **`config-service:`** — Wait for config-service.
  - **`condition: service_healthy`** — Don't just wait for it to START, wait until its healthcheck says HEALTHY.
  
**Why?** discovery-service doesn't depend on config-service in your current setup (eureka is standalone), but it's good practice. If you later add config import, it will be needed.

### Part 4: gateway-service

```yaml
  gateway-service:
    build:
      context: ./gateway-service
      dockerfile: Dockerfile
    container_name: gateway-service
    ports:
      - "8080:8080"
    networks:
      - ms-network
    depends_on:
      discovery-service:
        condition: service_healthy
```

**Why depends on discovery-service?** The gateway uses Eureka to discover other services. If Eureka isn't running, the gateway can't route requests.

### Part 5: client-service

```yaml
  client-service:
    build:
      context: ./client-service
      dockerfile: Dockerfile
    container_name: client-service
    ports:
      - "8081:8081"
    networks:
      - ms-network
    depends_on:
      config-service:
        condition: service_healthy
      discovery-service:
        condition: service_healthy
    environment:
      - SPRING_CONFIG_IMPORT=configserver:http://config-service:9999
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/
```

**New concept — `environment`**:

- **`environment:`** — Set environment variables inside the container.
- **`SPRING_CONFIG_IMPORT=configserver:http://config-service:9999`**
  - Your `application.yml` says `config.import: configserver:http://localhost:9999`
  - But inside Docker, `localhost` means "this container itself", NOT your machine!
  - We override it to `config-service:9999` — Docker resolves `config-service` to the IP of the config-service container (because they're on the same network).
  - **This is the #1 beginner mistake**: forgetting to replace `localhost` with the service name.

- **`EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/`**
  - Same idea. Tell the client to find Eureka at `discovery-service:8761` instead of `localhost:8761`.

> **IMPORTANT: Spring Boot converts environment variables like this:**
> `SPRING_CONFIG_IMPORT` → `spring.config.import`
> `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` → `eureka.client.serviceUrl.defaultZone`
> Dots become underscores, everything is uppercase. Spring Boot does this automatically.

### Part 6: produit-service

```yaml
  produit-service:
    build:
      context: ./produit_service
      dockerfile: Dockerfile
    container_name: produit-service
    ports:
      - "8082:8082"
    networks:
      - ms-network
    depends_on:
      config-service:
        condition: service_healthy
      discovery-service:
        condition: service_healthy
    environment:
      - SPRING_CONFIG_IMPORT=configserver:http://config-service:9999
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/
```

**Note**: `context: ./produit_service` — uses underscore because your folder is named `produit_service`.

---

## 10. Adding MySQL with Persistent Volumes

MySQL is different from your services: you don't BUILD it, you PULL a pre-made image.

```yaml
  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: efacture_db
      MYSQL_USER: efacture
      MYSQL_PASSWORD: efacture_pass
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - ms-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
```

**Line-by-line**:

- **`image: mysql:8.0`** — Instead of `build:`, we use `image:`. This tells Docker: "Don't build anything — pull the official MySQL 8.0 image from Docker Hub."

- **`environment:`** — MySQL's official image reads these variables on first startup:
  - `MYSQL_ROOT_PASSWORD` = Password for the root user (required!)
  - `MYSQL_DATABASE` = Create this database automatically on startup
  - `MYSQL_USER` = Create this user automatically
  - `MYSQL_PASSWORD` = Password for that user

- **`volumes:`** — This is critical!
  - **`mysql_data:/var/lib/mysql`** — Mount a named volume called `mysql_data` to `/var/lib/mysql` inside the container.
  - `/var/lib/mysql` is where MySQL stores all its data files.
  - **Without this**: When you stop the container, ALL your data is LOST.
  - **With this**: Data is saved in a Docker volume on your machine. When you restart, data is still there.

### What is a Volume?

```
Without Volume:                    With Volume:
─────────────────                  ─────────────────
Container starts → data created    Container starts → data saved to volume
Container stops  → DATA GONE! ❌   Container stops  → data safe in volume ✅
Container starts → empty again     Container starts → data restored from volume
```

A volume is like an external hard drive that Docker manages for you.

### Where is the volume stored on your machine?

Docker stores volumes in its own directory. You don't need to know the exact path.
To list volumes: `docker volume ls`
To inspect a volume: `docker volume inspect mysql_data`

---

## 11. Adding Prometheus

Prometheus is a monitoring tool that **scrapes** (collects) metrics from your services.

### Step 1: Create the Prometheus config file

Before adding Prometheus to compose, you need to tell it WHAT to monitor.

1. Create a folder: `e-facture-ms/monitoring/`
2. Create a file: `e-facture-ms/monitoring/prometheus.yml`
3. Put this in it:

```yaml
global:
  scrape_interval: 15s       # How often to collect metrics (every 15 seconds)

scrape_configs:               # Define WHAT to monitor

  - job_name: 'config-service'                    # A label for this group
    metrics_path: '/actuator/prometheus'            # The URL path to scrape
    static_configs:
      - targets: ['config-service:9999']           # hostname:port (Docker service name!)

  - job_name: 'discovery-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['discovery-service:8761']

  - job_name: 'gateway-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['gateway-service:8080']

  - job_name: 'client-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['client-service:8081']

  - job_name: 'produit-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['produit-service:8082']
```

**Key points**:
- `scrape_interval: 15s` — Prometheus asks each service "how are you?" every 15 seconds.
- `metrics_path: '/actuator/prometheus'` — Spring Boot Actuator exposes metrics at this URL.
  (Your services need `spring-boot-starter-actuator` + `micrometer-registry-prometheus` dependencies for this to work.)
- `targets: ['config-service:9999']` — Again, we use Docker service names, not `localhost`.

### Step 2: Add Prometheus to docker-compose.yml

```yaml
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    networks:
      - ms-network
```

**Explanation**:

- **`image: prom/prometheus:latest`** — Pull the official Prometheus image.
- **`ports: "9090:9090"`** — Access Prometheus dashboard at `http://localhost:9090`.
- **`volumes:`** — Two volumes:
  - `./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro`
    - This is a **bind mount** (maps a file from YOUR machine into the container).
    - Left side: `./monitoring/prometheus.yml` = the config file you just created.
    - Right side: `/etc/prometheus/prometheus.yml` = where Prometheus expects its config.
    - `:ro` = Read-only. The container can read but not modify this file.
  - `prometheus_data:/prometheus` = A named volume for Prometheus to store its collected data.

---

## 12. Adding Grafana

Grafana is a dashboard tool that **visualizes** the metrics collected by Prometheus.

```yaml
  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - ms-network
    depends_on:
      - prometheus
```

**Explanation**:

- **`image: grafana/grafana:latest`** — Official Grafana image.
- **`ports: "3000:3000"`** — Dashboard at `http://localhost:3000`.
- **`GF_SECURITY_ADMIN_PASSWORD=admin`** — Sets the admin password. Default username is `admin`.
- **`grafana_data:/var/lib/grafana`** — Persist dashboards and settings.
- **`depends_on: prometheus`** — Start after Prometheus (Grafana needs it as a data source).

### After starting, connect Grafana to Prometheus:

1. Open `http://localhost:3000`
2. Login: `admin` / `admin`
3. Go to Configuration → Data Sources → Add data source
4. Choose "Prometheus"
5. URL: `http://prometheus:9090` (Docker service name, NOT localhost!)
6. Click "Save & Test"

---

## 13. Networks Explained

### Why do we need a custom network?

By default, Docker Compose creates a network for you. But we want to be explicit and use `ms-network`.

When containers are on the **same network**, they can reach each other using their **service names** as hostnames:
- `client-service` can call `http://config-service:9999`
- `gateway-service` can call `http://discovery-service:8761`
- Prometheus can scrape `http://client-service:8081/actuator/prometheus`

Without a network, containers are isolated and can't talk to each other.

### Define the network at the bottom of docker-compose.yml

```yaml
networks:
  ms-network:
    driver: bridge
```

- **`ms-network:`** — The name of our network.
- **`driver: bridge`** — The type of network. `bridge` is the default and most common. It creates an internal network on your machine that only Docker containers can access.

### Also define all volumes

```yaml
volumes:
  mysql_data:
  prometheus_data:
  grafana_data:
```

This tells Docker Compose: "These are named volumes. Create them if they don't exist."

---

## 14. The Final docker-compose.yml

Here's the complete file structure. **You should type it yourself** (not copy-paste) to learn it:

```yaml
services:

  # ============================================
  # INFRASTRUCTURE SERVICES (start first)
  # ============================================

  config-service:
    build:
      context: ./config-service
      dockerfile: Dockerfile
    container_name: config-service
    ports:
      - "9999:9999"
    networks:
      - ms-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9999/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5

  discovery-service:
    build:
      context: ./discovery-service
      dockerfile: Dockerfile
    container_name: discovery-service
    ports:
      - "8761:8761"
    networks:
      - ms-network
    depends_on:
      config-service:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5

  gateway-service:
    build:
      context: ./gateway-service
      dockerfile: Dockerfile
    container_name: gateway-service
    ports:
      - "8080:8080"
    networks:
      - ms-network
    depends_on:
      discovery-service:
        condition: service_healthy

  # ============================================
  # DATABASE
  # ============================================

  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: efacture_db
      MYSQL_USER: efacture
      MYSQL_PASSWORD: efacture_pass
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - ms-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ============================================
  # BUSINESS SERVICES
  # ============================================

  client-service:
    build:
      context: ./client-service
      dockerfile: Dockerfile
    container_name: client-service
    ports:
      - "8081:8081"
    networks:
      - ms-network
    depends_on:
      config-service:
        condition: service_healthy
      discovery-service:
        condition: service_healthy
      mysql:
        condition: service_healthy
    environment:
      - SPRING_CONFIG_IMPORT=configserver:http://config-service:9999
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/efacture_db
      - SPRING_DATASOURCE_USERNAME=efacture
      - SPRING_DATASOURCE_PASSWORD=efacture_pass

  produit-service:
    build:
      context: ./produit_service
      dockerfile: Dockerfile
    container_name: produit-service
    ports:
      - "8082:8082"
    networks:
      - ms-network
    depends_on:
      config-service:
        condition: service_healthy
      discovery-service:
        condition: service_healthy
      mysql:
        condition: service_healthy
    environment:
      - SPRING_CONFIG_IMPORT=configserver:http://config-service:9999
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/efacture_db
      - SPRING_DATASOURCE_USERNAME=efacture
      - SPRING_DATASOURCE_PASSWORD=efacture_pass

  # ============================================
  # MONITORING
  # ============================================

  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    networks:
      - ms-network

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - ms-network
    depends_on:
      - prometheus

# ============================================
# VOLUMES — persistent storage
# ============================================
volumes:
  mysql_data:       # MySQL data persists here
  prometheus_data:  # Prometheus metrics data
  grafana_data:     # Grafana dashboards & config

# ============================================
# NETWORKS — all containers communicate here
# ============================================
networks:
  ms-network:
    driver: bridge
```

---

## 15. Essential Docker Commands Cheatsheet

### Starting & Stopping

| Command | What it does |
|---------|-------------|
| `docker-compose up --build` | Build all images AND start all containers (foreground, you see all logs) |
| `docker-compose up --build -d` | Same but **detached** (runs in background, frees your terminal) |
| `docker-compose down` | Stop and remove all containers (volumes are KEPT) |
| `docker-compose down -v` | Stop, remove containers, AND delete volumes (DATA LOST!) |
| `docker-compose restart client-service` | Restart just one service |

### Viewing Logs

| Command | What it does |
|---------|-------------|
| `docker-compose logs` | Show logs for ALL services |
| `docker-compose logs -f` | Follow/stream logs in real-time (like `tail -f`) |
| `docker-compose logs -f client-service` | Follow logs for ONE specific service |
| `docker-compose logs --tail=50 mysql` | Show last 50 lines of MySQL logs |

### Inspecting

| Command | What it does |
|---------|-------------|
| `docker ps` | List all running containers |
| `docker ps -a` | List ALL containers (including stopped ones) |
| `docker images` | List all images on your machine |
| `docker network ls` | List all networks |
| `docker volume ls` | List all volumes |

### Debugging / Getting Inside a Container

| Command | What it does |
|---------|-------------|
| `docker exec -it mysql bash` | Open a bash shell INSIDE the mysql container |
| `docker exec -it mysql mysql -u root -p` | Open MySQL CLI inside the container |
| `docker exec -it client-service sh` | Open shell in client-service (if bash not available) |
| `docker-compose exec config-service curl http://localhost:9999/actuator/health` | Run a command inside a running container |

### Cleaning Up

| Command | What it does |
|---------|-------------|
| `docker system prune` | Remove all stopped containers, unused networks, dangling images |
| `docker system prune -a` | Remove EVERYTHING unused (⚠️ BE CAREFUL — removes all images too) |
| `docker volume prune` | Remove all unused volumes (⚠️ data loss) |
| `docker rmi efacture-config-service:dev` | Remove a specific image |

### Building

| Command | What it does |
|---------|-------------|
| `docker build -t myimage:tag .` | Build an image from a Dockerfile in current directory |
| `docker build -t myimage:tag -f other.Dockerfile .` | Build using a specific Dockerfile |
| `docker build --no-cache -t myimage:tag .` | Build WITHOUT using cache (forces fresh download) |

---

## 16. Troubleshooting Common Errors

### "localhost" doesn't work between containers

**Problem**: Your `application.yml` says `localhost:9999` but client-service can't reach config-service.

**Why**: Inside Docker, `localhost` means "this container". Each container is isolated.

**Fix**: Use the **service name** as hostname: `config-service:9999`. That's why we set `environment` variables in docker-compose.yml.

### Container exits immediately

**Problem**: `docker-compose up` starts a service, but it immediately stops.

**Debug**:
```bash
docker-compose logs client-service
```
Look for Java exceptions. Common causes:
- Can't connect to config-service (not ready yet) → Add `depends_on` with healthcheck
- Can't connect to MySQL (not ready yet) → Add `depends_on` with healthcheck
- Port already in use → Change the host port

### "port is already allocated"

**Problem**: Something else on your machine is using that port.

**Find what's using it (Windows)**:
```bash
netstat -ano | findstr :8080
```

**Fix**: Either stop that process, or change the HOST port in docker-compose.yml:
```yaml
ports:
  - "8888:8080"    # Use 8888 on your machine, 8080 inside container
```

### Build takes forever

**Problem**: Every rebuild downloads all Maven dependencies again.

**Why**: You probably changed the order of COPY commands in your Dockerfile.

**Fix**: Always copy `pom.xml` first and run `dependency:go-offline` BEFORE copying `src/`. This leverages Docker layer caching.

### MySQL data disappears after restart

**Problem**: You used `docker-compose down -v` (the `-v` flag deletes volumes!).

**Fix**: Use `docker-compose down` (without `-v`) to keep your data.

---

## Your Action Plan (What to Do Now)

Follow this exact order:

1. **Install Docker Desktop** (if not already installed): https://www.docker.com/products/docker-desktop/
2. **Create `config-service/Dockerfile`** — type it manually, referring to Section 5.
3. **Build and test it** — Section 6. Make sure it works alone before continuing.
4. **Create Dockerfiles for the other 4 services** — Section 7.
5. **Create `monitoring/prometheus.yml`** — Section 11, Step 1.
6. **Write `docker-compose.yml`** — Type it piece by piece following Sections 9-12.
7. **Run `docker-compose up --build`** — Watch the logs, fix errors.
8. **Test**: Open `http://localhost:8761` (Eureka), `http://localhost:9090` (Prometheus), `http://localhost:3000` (Grafana).

**Take it slow. One service at a time. Read the errors. You've got this.**
