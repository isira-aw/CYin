# Use OpenJDK 17 as the base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the compiled JAR file into the container
COPY target/registration-login-page.jar /app/registration-login-page.jar

# Expose port 8080 for the app
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "registration-login-page.jar"]
