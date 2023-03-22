FROM adoptopenjdk/openjdk15
COPY target/dist /usr/share/songscribe
WORKDIR /usr/share/songscribe
CMD ["java","-cp","SongScribe.jar","songscribe.WebServer"]
