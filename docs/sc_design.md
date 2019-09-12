## SpringCloud extension mechanism
SPI is an abbreviation of Service Provider Interface, which is equivalent to a specification and is a service provisioning discovery mechanism built into JDK. As long as the interface is defined in the specified directory, the jar package that conforms to the specification can be imported, which is very suitable for third-party developers to extend.

Of course, SPI also has its own problems. The JDK's own SPI implementation instantiates all extension points at once, which can cause applications to start longer and waste unnecessary resources.

Spring is an extension of the SPI mechanism. Spring is so popular now, thanks to Spring's almost paranoid open concept. Unlike many other frameworks, Spring emphasizes openness, or scalability. In Spring's family bucket, many components are integrated from the outside, and various starters are collections of these components. For example, service discovery can be implemented using Consul, Eureka, ZooKeeper, Etcd, and the like.

you know , Spring Cloud depend on Spring Boot. Spring Boot depend on Spring. The scalability of Spring Cloud comes from @EnableAutoConfiguration in Spring Boot. The general loading steps are as follows:
 
1. When SpringBoot starts, it will scan the META-INF/spring.factories files in all Jars under the classpath;
2. Read the specified Configuration and automatically create the bean according to condition on the Configuration.
3. Put into the Spring Context, this will inject the container, you can directly inject it anywhere.