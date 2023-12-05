
FROM eclipse-temurin:17-jre-alpine
#RUN apt-get update -y && \
#	apt-get upgrade -y && \
#	apt-get dist-upgrade -y && \
#	apt-get -y autoremove && \
#	apt-get clean

#RUN apt-get install zip unzip

RUN apk add --no-cache wget unzip
RUN apk add --no-cache graphviz

RUN mkdir /app

# RUN wget https://testoptimal.com/downloads/Rel-6.0/TestOptimal_6.0.28_linux.zip
COPY build/TestOptimal_7.0.3_mac.zip /app/TestOptimal.zip

RUN unzip /app/TestOptimal.zip -d /app
RUN chmod +x /app/startTestOptimalServer.sh
RUN ls -al /app/TestOptimal.jar

WORKDIR /app
RUN cd /app
CMD ["java --version"]

ENTRYPOINT ["java","-jar","/app/TestOptimal.jar", "-classpath", "lib/"]
#ENTRYPOINT ["sh", "/app/startTestOptimalServer.sh"] 
