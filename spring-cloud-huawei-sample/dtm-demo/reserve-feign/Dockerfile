FROM openjdk:8-jre-alpine
ENV APP_ROOT=/root/servicestage/reserve-feign/
ENV LOG_ROOT=/var/log/reserve-feign/
RUN mkdir -p $APP_ROOT
RUN mkdir -p $LOG_ROOT

COPY target/reserve-feign-1.3.3-Hoxton-SNAPSHOT/reserve-feign-1.3.3-Hoxton-SNAPSHOT $APP_ROOT


RUN cd $APP_ROOT && chmod -R 770 .
RUN chmod +x /root/servicestage/reserve-feign/bin/start.sh

ENTRYPOINT ["sh", "/root/servicestage/reserve-feign/bin/start.sh"]
