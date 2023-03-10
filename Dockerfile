FROM maven:3.6.1-jdk-8 as maven_builder
WORKDIR /library-single
ADD pom.xml .
ADD src ./src
RUN mvn clean package

FROM tomcat:8.5.43-jdk8
COPY --from=maven_builder /library-single/target/library-rest.war /usr/local/tomcat/webapps