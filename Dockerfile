FROM artifacts.msap.io/mulesoft/core-paas-base-image-openjdk-8:v4.1.21 as PRODUCTION

# Create workdir where application is going to be located
WORKDIR /usr/src/app/

COPY . .

# Command that starts the app
CMD ["bash", "-c", "exec java -XX:ActiveProcessorCount=1 -jar ./target/amf-learning-1.0-SNAPSHOT-jar-with-dependencies.jar"]
