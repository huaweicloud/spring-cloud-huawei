## 运行demo
通过项目中的示例开始分布式配置。spring-cloud-huawei-sample/config-demo用来模拟spring cloud 
工程使用华为云ServiceStage分布式配置的场景：


## 在华为云上运行Demo

### 前提条件

已经在[huaweicloud](https://www.huaweicloud.com)上开通账号，并且在[ServiceStage](https://www.huaweicloud.com/product/servicestage.html)上添加了ServiceComb微服务引擎。

### step 1 构建工程

因为spring-cloud-huawei还没有发布到公共仓库，如果要使用，需要先下载代码在本地构建。
    
    mvn clean install --settings .maven.settings.xml
    
### step 2 在ServiceStage中新建相关配置，如demo中的dd

### step 3 修改配置文件，并且启动服务

    spring:
      application:
        name: price
      cloud:
        servicecomb:
          config:
            serverAddr: https://cse.cn-east-3.myhuaweicloud.com 
            watch:
              delay: 10000

### step 4 验证
访问http://127.0.0.1:8080/price?id=11
页面显示dd在配置中心配置的值