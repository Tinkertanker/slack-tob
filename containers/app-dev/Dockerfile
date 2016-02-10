FROM java:openjdk-8u72-jre
RUN mkdir /usr/src/tob
COPY . /usr/src/tob
WORKDIR /usr/src/tob
CMD ["java", "-jar", "target/project.jar"]