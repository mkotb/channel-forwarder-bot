FROM openjdk:11.0-slim

WORKDIR /bot

COPY ./build/libs/ChannelForwarderBot.jar bot.jar
CMD java -jar bot.jar