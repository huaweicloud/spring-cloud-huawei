FROM openjdk:8-jre-alpine
ENV APP_ROOT=/root/servicestage/coupon/
ENV LOG_ROOT=/var/log/coupon/
RUN mkdir -p $APP_ROOT
RUN mkdir -p $LOG_ROOT

COPY target/coupon-1.2.0/coupon-1.2.0 $APP_ROOT


RUN cd $APP_ROOT && chmod -R 770 .
RUN chmod +x /root/servicestage/coupon/bin/start.sh

ENTRYPOINT ["sh", "/root/servicestage/coupon/bin/start.sh"]
