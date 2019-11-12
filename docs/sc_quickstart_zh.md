#快速开始
spring-cloud-huawei可以使用spring cloud的方式注册发现，只需要修改部分配置文件即可。

## step 1 引用jar依赖

因为spring-cloud-huawei还没有发布到公共仓库，如果要使用，需要先下载代码在本地构建。

mvn clean install --settings .maven.settings.xml 
以下以maven为例。 项目中可以使用dependencyManagement引入依赖。

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

## step 2 新建Project或module，定义配置文件。

通过配置文件application.yml)定义配置

spring:
  application:
    name: price
  cloud:
    servicecomb:
      config:
        serverAddr: https://cse.cn-east-3.myhuaweicloud.com 
        watch:
          delay: 10000
## step 3 启动类添加注解


	@SpringBootApplication
	@EnableDiscoveryClient
	public class Application {
	  public static void main(String[] args) {
	    SpringApplication.run(Application.class, args);
	  }
	}

