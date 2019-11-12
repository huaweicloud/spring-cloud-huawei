## 快速开始

### 前提条件

已经在[huaweicloud](https://www.huaweicloud.com)上开通账号，并且在[ServiceStage](https://www.huaweicloud.com/product/servicestage.html)上添加了ServiceComb微服务引擎。

### step 1 引用jar依赖

因为spring-cloud-huawei还没有发布到公共仓库，如果要使用，需要先下载代码在本地构建。
    
    mvn clean install --settings .maven.settings.xml 

以下以maven为例。
项目中可以使用dependencyManagement引入依赖。

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

或者修改maven的配置文件setting.xml，添加华为云repository和pluginRepository

	  <mirrors>
		<mirror>
			<id>huaweicloud</id>
			<mirrorOf>*,!HuaweiCloudSDK</mirrorOf>
			<url>https://repo.huaweicloud.com/repository/maven/</url>
		</mirror>  
	  </mirrors>
	
	  <profiles>
		<profile>
			<id>MyProfile</id>
			<pluginRepositories>
				<pluginRepository>
				  <id>HuaweiCloudSDK</id>
				  <url>https://repo.huaweicloud.com/repository/maven/huaweicloudsdk/</url>
				  <releases>
					<enabled>true</enabled>
				  </releases>
				  <snapshots>
					<enabled>true</enabled>
				  </snapshots>
				</pluginRepository>
			</pluginRepositories>
			<repositories>
				<repository>
					<id>HuaweiCloudSDK</id>
					<url>https://repo.huaweicloud.com/repository/maven/huaweicloudsdk/</url>
					<releases>
						<enabled>true</enabled>
					</releases>
					<snapshots>
						<enabled>false</enabled>
					</snapshots>
				</repository>
			</repositories>
		</profile>
	  </profiles>
	  
	  <activeProfiles>
	    <activeProfile>MyProfile</activeProfile>
	  </activeProfiles>

引入spring-cloud-starter-huawei-servicecomb-discovery，用于注册发现服务
    
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-huawei-servicecomb-discovery</artifactId>
    </dependency>

引入spring-cloud-starter-huawei-config，用于分布式配置

    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-huawei-config</artifactId>
    </dependency>
    

  
  
### step 2 新建Project或module，定义配置文件。
通过配置文件(bootstrap.yml)定义配置，在spring cloud中，bootstrap.yml的启动优先级高于application.yml

    spring:
      application:
        name: price
      cloud:
        servicecomb:
          config:
            serverAddr: https://cse.cn-east-3.myhuaweicloud.com 
            watch:
              delay: 10000

    
### step 3 通过注解@Value取得配置中心的值

      @Value("${server.port}")
