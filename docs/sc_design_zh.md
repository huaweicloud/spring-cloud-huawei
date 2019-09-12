
## SpringCloud 的扩展机制
SPI是Service Provider Interface的缩写，相当于一种规范，是JDK内置的一种服务提供发现机制。只要在指定的目录定义好接口，引入符合规范的jar包就可以工作了，非常符合第三方开发者进行扩展。

当然，SPI也有自身的问题。 JDK自带的SPI实现会一次性实例化所有扩展点，这会导致应用启动的时间变长，并且浪费不必要的资源。

Spring就是采用的SPI机制进行扩展。 Spring如今的大红大紫，绝对和Spring近乎偏执的理念有关，与其他很多框架不同，Spring更多强调的是开放性，或者说是扩展性。在Spring的全家桶中，很多组件都是从外部集成进来的，各种starter就是这些组件的集合。如注册发现，可以使用Consul、Eureka、ZooKeeper、Etcd等实现。

我们都知道，Spring Cloud依赖于Spring Boot，Spring Boot又依赖于Spring，Spring Cloud的扩展性来源于Spring Boot中的@EnableAutoConfiguration， 大致的加载步骤如下：
 
1. SpringBoot启动的时候，会扫描classpath下所有Jar中META-INF/spring.factories文件； 
2. 读取指定的Configuration，根据Configuration上的Conditional条件自动创建bean； 
3. 放入Spring Context中，这样就注入了容器，任何地方都可以直接注入使用了。
