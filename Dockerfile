FROM openjdk:8
EXPOSE 8080
ADD target/3-candles-docker.jar 3-candles-docker.jar
ENTRYPOINT ["java","-jar","/3-candles-docker.jar"]