### 异构应用指南

本demo使用java chassis、go chassis 、spring cloud huawei 展示异构应用的场景下
，采用不同框架开发的微服务如何实现互相调用。

调用接口：http://127.0.0.1:8081/longCall 实现长调用

调用链路：
用户(调用者) -> spring cloud -> java chassis -> spring cloud
                             |             |-> go chassis
                             |
                            -> go chassis -> spring cloud
                                         |-> java chassis

