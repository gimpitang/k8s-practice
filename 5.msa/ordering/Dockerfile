# 필요 프로그램 설치
FROM openjdk:17-jdk-alpine as stage1

# 파일 복사(필요한 파일)
WORKDIR /app
COPY gradle gradle
COPY src src
#파일명이라서 . 으로 갈음 가능
COPY build.gradle .
COPY gradlew .
COPY settings.gradle .

# 빌드
RUN chmod 777 gradlew
RUN ./gradlew bootJar

# 두번째 스테이지
# 필요 프로그램 설치
FROM openjdk:17-jdk-alpine
WORKDIR /app

# COPY --from=(가져올 from 위치) (실행할 파일의 위치) (내가 설정할 파일 이름)
COPY --from=stage1 /app/build/libs/ordersystem-0.0.1-SNAPSHOT.jar app.jar
# COPY --from=stage1 /app/build/libs/*.jar app.jar  --> 이것도 가능능

# 실행 : CMD 또는 ENTRYPOINT를 통해 컨테이너를 배열 형태의 명령어로 실행
# 실행프로그램 (java),=
# ENTRYPOINT ["java", "-jar", "/app/build/libs/ordersystem-0.0.1-SNAPSHOT.jar"] --> 위에서 복사 했기때문에
# app.jar 말고 *.jar 가능
ENTRYPOINT ["java", "-jar", "app.jar"]

# 도커 이미지 만드는 명령어
# docker build -t my-spring:v1.0 .

# 도커 실행 명령어(이미지가 없어도 자동으로 이미지 생성하면서 실행함.)
# 우선 로컬에서 이미지를 먼저 찾아보고 도커허브에서 이미지를 찾아서 실행해버림.
# docker run --name (컨테이너명) -p (내포트:yml이 설정한 이미지의 포트(지금의 경우 spring)) (이미지명)
# docker run --name my-spring -p 8080:8080 my-spring:v1.0

# 지금은 db없어서 localhost를 못찾지만 강제로 실행하게 된다.
# 원래 yml파일에 있던 localhost 부분을 host.docker.internal 이걸로 바꿈

# host에 yml에 있는 datasorc를 덮어쓰기 하는 작업
#docker run --name my-spring -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/ordersystem my-spring:v1.0

# 
#docker run --name my-spring -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/ordersystem -e SPRING_REDIS_HOST=host.docker.internal my-spring:v1.0
# docker run --name my-spring -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/ordersystem -e SPRING_REDIS_HOST=host.docker.internal my-spring:v1.0


# 이제 도커 컴포즈로 이동