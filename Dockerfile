FROM maven:3.6.1-jdk-8 as maven_builder
WORKDIR /library-single
ADD pom.xml .
ADD src ./src
RUN mvn clean package

FROM openjdk:8
COPY --from=maven_builder /library-single/target/library-single-0.1-jar-with-dependencies.jar .
CMD ["java", "-jar", "library-single-0.1-jar-with-dependencies.jar"]