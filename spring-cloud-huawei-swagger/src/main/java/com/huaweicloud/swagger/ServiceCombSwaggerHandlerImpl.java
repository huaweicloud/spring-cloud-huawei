package com.huaweicloud.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

/**
 * @Author GuoYl123
 * @Date 2019/12/17
 **/
public class ServiceCombSwaggerHandlerImpl implements ServiceCombSwaggerHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombSwaggerHandlerImpl.class);
  @Autowired
  private DocumentationCache documentationCache;

  @Autowired
  private ServiceModelToSwagger2Mapper mapper;

  @Autowired
  private ServiceCombClient serviceCombClient;

  private Map<String, Swagger> swaggerMap = new HashMap<>();

  @Value("${withJavaChassis:false}")
  private boolean isSync;

  /**
   * 拆分注册
   */
  public void init() {
    Documentation documentation = documentationCache
        .documentationByGroup(Docket.DEFAULT_GROUP_NAME);
    if (documentation == null) {
      LOGGER.warn("Unable to find specification for group {}", Docket.DEFAULT_GROUP_NAME);
    }
    Multimap<String, ApiListing> allList = documentation.getApiListings();
    Iterator iter = allList.entries().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, ApiListing> entry = (Map.Entry) iter.next();
      Multimap<String, ApiListing> tempList = HashMultimap.create();
      tempList.put(entry.getKey(), entry.getValue());
      try {
        Field field = Documentation.class.getDeclaredField("apiListings");
        field.setAccessible(true);
        field.set(documentation, tempList);
        Swagger temSwagger = mapper.mapDocumentation(documentation);
        temSwagger.setTags(null);
        swaggerMap.put(entry.getKey(), temSwagger);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return;
  }

  /**
   * 流程可以和java-chassis不同: spring-cloud不依赖schema schema注册调用接口可以改为异步 tips:在和java-chassis组网场景下需要同步加载
   * todo: schema生成理论上也可改为异步，但基于spring-fox，要看下能否实现
   *
   * @param microserviceId
   * @param schemaIds
   */
  public void registerSwagger(String microserviceId, List<String> schemaIds) {
    if (isSync) {
      registerSwaggerSync(microserviceId, schemaIds);
    } else {
      registerSwaggerAsync(microserviceId, schemaIds);
    }
  }

  public List<String> getSchemas() {
    return new ArrayList<>(swaggerMap.keySet());
  }

  /**
   * todo: 异常处理待完善
   *
   * @param microserviceId
   * @param schemaIds
   */
  private void registerSwaggerSync(String microserviceId, List<String> schemaIds) {
    schemaIds.forEach(schemaId -> {
      try {
        String str = Yaml.mapper().writeValueAsString(swaggerMap.get(schemaId));
        serviceCombClient.registerSchema(microserviceId, schemaId, str);
      } catch (RemoteOperationException e) {
        LOGGER.error("registerSwagger server failed : {}", e.getMessage());
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    });
  }

  private void registerSwaggerAsync(String microserviceId, List<String> schemaIds) {
    Executors.newSingleThreadExecutor().execute(() -> {
      registerSwaggerSync(microserviceId, schemaIds);
    });
  }
}
