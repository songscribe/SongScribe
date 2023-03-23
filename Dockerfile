FROM adoptopenjdk/openjdk15:ubuntu
COPY target/dist /usr/share/songscribe
WORKDIR /usr/share/songscribe
ENTRYPOINT ["java","-cp","SongScribe.jar","songscribe.WebServer"]
