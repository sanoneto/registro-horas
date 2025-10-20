FROM eclipse-temurin:23-jre
ARG APP_JAR=target/*.jar
WORKDIR /app

# Se você preferir que o workflow gere o JAR (mvn package no CI),
# comente as linhas abaixo e use apenas a cópia do JAR gerado pelo CI:
# COPY --from=build /workspace/target/*.jar /app/app.jar

# Caso o seu CI já rodou "mvn package" e gerou target/*.jar no contexto do build,
# o passo abaixo copia o jar diretamente do contexto (útil quando workflow roda mvn antes do docker build)
COPY ${APP_JAR} /app/app.jar

# Expor porta (ajuste se necessário)
EXPOSE 8080

# Variáveis de ambiente com valores padrão (podem ser sobrescritas em tempo de execução)
ENV SPRING_DATASOURCE_URL="jdbc:postgresql://db:5432/postgres"
ENV SPRING_DATASOURCE_USERNAME="postgres"
ENV SPRING_DATASOURCE_PASSWORD="admin"

# Opções de JVM recomendadas (ajuste conforme necessidade)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"

# Comando para rodar o jar
ENTRYPOINT [ "/bin/sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar" ]