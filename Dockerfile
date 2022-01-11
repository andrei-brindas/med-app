FROM openjdk:17

#Environment vars
ENV PROFILE=default
ENV PORT=5526
ENV JWT_SECURITY_KEY=secret


#Copy app
COPY ./target/*.jar app.jar
COPY runScript.sh runScript.sh

#Run app with envirement
RUN chmod +x runScript.sh
ENTRYPOINT ["/runScript.sh"]
