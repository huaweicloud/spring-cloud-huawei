## 运行demo
通过项目中的示例开始分布式事务。spring-cloud-huawei-sample/dtm-demo下提供了三个工程，用来模拟spring cloud 
工程使用华为云ServiceStage分布式事务的场景：

* reserve：预订服务使用RestTemplate，事务发起方。
* reserve-feign：预订服务使用Feign，事务发起方。
* coupon：优惠券服务，分支事务。
* ticket：票务服务，分支事务。

## 在华为云上运行Demo

### 前提条件

已经在[huaweicloud](https://www.huaweicloud.com)上开通账号，并且在[ServiceStage](https://www.huaweicloud.com/product/servicestage.html)上添加了微服务引擎和分布式事务引擎。

### step 1 构建工程

因为spring-cloud-huawei还没有发布到公共仓库，如果要使用，需要先下载代码在本地构建。
    
    mvn clean install --settings .maven.settings.xml

### step 2 修改配置文件

    dtm:
      appName: reserve  ##应用名称
      rpc:
        sslEnabled: true  ##开启SSL验证，华为云目前必须开启
      proxy:
        endpoint: https://192.168.0.207:30125 ##dtm服务的地址，在ServiceStage上创建dtm引擎成功后就会显示

### step 3 生成docker镜像，并推送到镜像仓库
以reverse为例

    cd spring-cloud-huawei/spring-cloud-huawei-sample/dtm-demo/reserve
    docker build -t reserve:0.1 .
    
打tag，这里需要跟云上的路径和组织相对应
    
    docker tag reserve:0.1  swr.cn-north-4.myhuaweicloud.com/wang/reserve:0.1

推送到镜像仓库
  
    docker push swr.cn-north-4.myhuaweicloud.com/wang/reserve:0.1
    
### step 4 创建应用
以reserve为例，coupon和ticket部署方式相同。
1. 进入应用列表创建应用，选择ServiceComb引擎，点击下一步
2. 运行环境选择docker，点击下一步
3. 镜像仓库选择 step 3 生成的镜像。点击外网访问按钮，端口填写8080，点击下一步
4. 检查配置项，点击下一步创建应用
5. 创建完成后，查看日志，验证应用启动情况

  更详细的说明可以参考[华为云官方文档说明](https://support.huaweicloud.com/usermanual-servicestage/servicestage_user_0115.html)

### step 5 验证事务

  通过生成的域名地址访问应用
  
      http://xxxx/reverse?id=1
  
  正常页面会返回成功，日志中会有响应的日志记录。
  
  查看dtm控制台，历史事务，会有相应的事务提交记录
  
  ![avatar](./imgs/dtm_history.png)
  
  如果要验证回滚，可以将ticket或者coupon停掉，活跃事务中会看到事务一直在等待重试，
  当停掉的事务重新启动后，事务会再次提交，示例中可以在日志中看到。
  
  如果要验证超时，可以输入
      
      http://xxxx/reverse?id=sleep
   
  ticket会一直在Confirm方法中阻塞，同样可以通过日志和dtm控制台看到相应情况。

### 如何通过RestTemplate使用DTM分布式事务

如果使用RestTemplate实现DTM分布式事务，代码仅需要增加注解。
通过注解DTMTxBegin定义全局事务

    @DTMTxBegin(appName = "reserve")

通过注解DTMTccBranch定义分支事务，放到"try"方法上。通过confirmMethod和cancelMethod定义确认和回滚方法

      @GetMapping(value = "/discountCoupon")
      @DTMTccBranch(identifier = "coupon", confirmMethod = "confirm", cancelMethod = "cancel")
      public void discountCoupon() throws InterruptedException {
        //try,预留资源，判断是否可以执行。如库存服务，可以在数据库中增加字段，预减库存
      }
    
      public void confirm() {
        //confirm，可以理解为事务提交，如库存服务，真正的把预留的库存扣掉
      }
    
      public void cancel() {
        //cancel,回滚方法，当出现异常的时候调用此方法释放资源，如库存服务，释放预留库存
      }

### 如何通过Feign使用DTM分布式事务

与RestTemplate用法相同