# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from target directory to the container
COPY target/*.jar app.jar

# Expose the application port (default Spring Boot port is 8080)
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]
