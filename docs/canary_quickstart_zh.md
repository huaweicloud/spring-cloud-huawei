## 快速开始

### step 1 引用jar依赖

引入spring-cloud-starter-huawei-router，用于灰度发布

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-huawei-router</artifactId>
        </dependency>
  
### step 2 配置灰度发布规则
通过配置文件(application.yml)定义

[规则文档参考](https://docs.go-chassis.com/user-guides/router.html)

#### 主要的流程阶段可以抽象为两个部分：

Match：
1.	读取服务名匹配的目标服务(provider)下的所有规则。
2.	根据优先级和match中的header来确定规则。
3.	Match结束，选择出唯一的route规则，进入下阶段。
(无匹配match则灰度不生效)

Distribute:
1.	根据weight的比例分配流量到不同version，tags的实例上。
(无匹配tags走最新版本)
2.	选择出符合条件的server，进行调用。

*tips:* 为服务实例配置tags：

    instance_description:
      properties:
        tags:
          tag_key: tag_value

启动服务，流量即可按照既定规则分配。
