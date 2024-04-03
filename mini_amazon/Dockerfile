FROM openjdk:17-jdk-slim
RUN mkdir /app
WORKDIR /app
# COPY . .
# RUN ./gradlew build
COPY ./build/libs/final_project-1.0-SNAPSHOT.jar final_project-1.0-SNAPSHOT.jar
ENTRYPOINT ["sh","-c","sleep 3 && java -jar final_project-1.0-SNAPSHOT.jar"]
