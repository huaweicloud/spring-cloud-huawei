FROM openjdk:8-jre-alpine
ENV APP_ROOT=/root/servicestage/ticket/
ENV LOG_ROOT=/var/log/ticket/
RUN mkdir -p $APP_ROOT
RUN mkdir -p $LOG_ROOT

COPY target/ticket-0.0.3/ticket-0.0.3 $APP_ROOT


RUN cd $APP_ROOT && chmod -R 770 .
RUN chmod +x /root/servicestage/ticket/bin/start.sh

ENTRYPOINT ["sh", "/root/servicestage/ticket/bin/start.sh"]
