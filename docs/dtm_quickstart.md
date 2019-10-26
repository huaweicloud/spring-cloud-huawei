## Quick Start

### Condition

The account has been opened on [huaweicloud](https://www.huaweicloud.com), and the engine of microservice and dtm have been added to [ServiceStage](https://www.huaweicloud.com/product/servicestage.html). 
### step 1 add jar

Because spring-cloud-huawei has not been released to the public repository, if you want to use it, you need to download the code to build locally.
    
    mvn clean install --settings .maven.settings.xml
    
An example of maven for you .
dependencyManagement can be used in projects to manage dependencies.

    <dependencyManagement>
      <dependencies>
        <dependency>
          <groupId>org.springframework.cloud.huawei</groupId>
          <artifactId>spring-cloud-huawei-dependencies</artifactId>
          <version>${project.version}</version>
          <type>pom</type>
          <scope>import</scope>
        </dependency>
      </dependencies>
    </dependencyManagement>

add spring-cloud-starter-huawei-servicecomb-discovery for discovery.
    
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-huawei-servicecomb-discovery</artifactId>
    </dependency>

add spring-cloud-starter-huawei-dtm for distributed transaction

    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-huawei-dtm</artifactId>
    </dependency>
    

  
  
### step 2 Create a new Project or module, define a global transaction, and support for calling branch transactions through the RestTemplate and Feign modes.
Define transaction information through configuration file (application.yml)

    dtm:
      appName: reserve 
      rpc:
        sslEnabled: true 
      proxy:
        endpoint: https://192.168.0.5:30125 #dtm server address，get it from dtm engine

Define global transactions by DTMTxBegin

    @DTMTxBegin(appName = "reserve")
    
### step 3 Create a new Project or module to define branch transactions
Similarly, define transaction information through the configuration file (application.yml)

    dtm:
      appName: coupon 
      rpc:
        sslEnabled: true 
      proxy:
        endpoint: https://192.168.0.5:30125 #dtm server address，get it from dtm engine

Define the branch transaction by DTMTccBranch and put it on the "try" method. Confirm and rollback methods with confirmMethod and cancelMethod

      @GetMapping(value = "/discountCoupon")
      @DTMTccBranch(identifier = "coupon", confirmMethod = "confirm", cancelMethod = "cancel")
      public void discountCoupon() throws InterruptedException {
        //try,Reserve resources to determine whether they can be executed. Such as inventory services, you can add columns in the database, pre-reduction of inventory
      }
    
      public void confirm() {
        //confirm，Can be understood as transaction submit, such as inventory services, really deducted reserved inventory
      }
    
      public void cancel() {
        //cancel,Rollback method, call this method to release resources when an exception occurs, such as inventory service, release reserved inventory
      }
### step 4 View transaction status on the huaweicloud distributed transaction control panel.

  ![avatar](./imgs/dtm_history.png)