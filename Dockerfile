
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget unzip
RUN apk add --no-cache graphviz
RUN apk add firefox

RUN mkdir /app

RUN wget https://testoptimal.com/downloads/Rel-7.0/TestOptimal_7.0.3_mac.zip -O /app/TestOptimal.zip
#COPY build/TestOptimal_7.0.3_mac.zip /app/TestOptimal.zip

RUN unzip /app/TestOptimal.zip -d /app

WORKDIR /app
RUN cd /app

ENTRYPOINT ["java","-jar","/app/TestOptimal.jar", "-classpath", "lib/", "--server.port=8888"]
