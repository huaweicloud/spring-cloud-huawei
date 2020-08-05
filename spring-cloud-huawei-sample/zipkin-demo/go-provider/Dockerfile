# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM golang:1.12.10 as builder

ADD . /go-provider/
WORKDIR /go-provider/
ENV GOPROXY=https://goproxy.io
RUN go build -a github.com/huaweicloud/spring-cloud-huawei/tree/master/spring-cloud-huawei-sample/zipkin-demo/go-provider/main


FROM frolvlad/alpine-glibc:latest
RUN mkdir -p /opt/order

ENV CHASSIS_HOME=/opt/go-provider/

COPY --from=builder /order/go-provider $CHASSIS_HOME
COPY conf/microservice.yaml conf/chassis.yaml $CHASSIS_HOME/conf/
WORKDIR $CHASSIS_HOME
ENTRYPOINT ["/opt/go-provider/go-provider"]