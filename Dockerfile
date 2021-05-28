FROM maven:3.8-openjdk-17 AS MAVEN_BUILD
COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build/
RUN mvn dependency:go-offline -B
RUN mvn package

FROM openjdk:17
WORKDIR /app
COPY --from=MAVEN_BUILD /build/target/*.jar /app/app.jar
EXPOSE 80 8080 3306
ENTRYPOINT ["java", "-jar", "app.jar"]

# COPY --from=MAVEN_BUILD /build/target/*.war /app/app.war
# ENTRYPOINT ["java", "-jar", "app.war"]
