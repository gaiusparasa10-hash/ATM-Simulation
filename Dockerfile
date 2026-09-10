FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY src ./src
COPY frontend ./frontend
COPY lib ./lib

RUN mkdir -p out \
    && find src -name "*.java" > sources.txt \
    && javac --add-modules jdk.httpserver \
       -cp "lib/mysql-connector-j-26.7.0.jar" \
       -d out @sources.txt

CMD ["sh", "-c", "java --add-modules jdk.httpserver -cp 'out:lib/mysql-connector-j-26.7.0.jar' Main"]