FROM maven:3-amazoncorretto-17 AS builder
VOLUME /root/.m2
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn install

FROM amazoncorretto:18-alpine-jdk AS runner
WORKDIR /app
COPY --from=builder /app/target/solver-0.0.1-jar-with-dependencies.jar ./solver.jar
CMD ["java","-jar","solver.jar"]