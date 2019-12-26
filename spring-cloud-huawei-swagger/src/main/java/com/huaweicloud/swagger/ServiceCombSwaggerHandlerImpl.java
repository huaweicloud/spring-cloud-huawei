package com.huaweicloud.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import io.swagger.models.AbstractModel;
import io.swagger.models.Info;
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

  private String TITLE_PREFIX = "swagger definition for ";

  private String X_JAVA_INTERFACE_PREFIX = "cse.gen.";

  private String X_JAVA_INTERFACE = "x-java-interface";

  private String INTF_SUFFIX = "Intf";

  /**
   * Split registration
   */
  public void init(String appName, String serviceName) {
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
        String className = genClassName(entry.getKey());
        String xInterfaceName = genXInterfaceName(appName, serviceName, entry.getKey());
        Field field = Documentation.class.getDeclaredField("apiListings");
        field.setAccessible(true);
        field.set(documentation, tempList);
        Swagger temSwagger = mapper.mapDocumentation(documentation);
        Info info = temSwagger.getInfo();
        info.setTitle(TITLE_PREFIX + className);
        info.setVendorExtension(X_JAVA_INTERFACE, xInterfaceName);
        temSwagger.setInfo(info);
        //todo : add itself tag
        temSwagger.setTags(null);
        temSwagger.getDefinitions().forEach((k, v) -> {
          if (v instanceof AbstractModel) {
            ((AbstractModel) v).setVendorExtension(X_JAVA_INTERFACE, genXDefinitionName(k));
          }
        });
//        temSwagger.setVendorExtension("consumes","application/json");
//        temSwagger.setVendorExtension("produces","application/json");
        swaggerMap.put(entry.getKey(), temSwagger);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return;
  }

  /**
   * schema注册调用接口可以改为异步,在和java-chassis组网场景下需要同步加载 todo: schema生成理论上也可改为异步，但基于spring-fox，要看下能否实现
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
   * @param microserviceId
   * @param schemaIds
   */
  private void registerSwaggerSync(String microserviceId, List<String> schemaIds) {
    schemaIds.forEach(schemaId -> {
      try {
        String str = Yaml.mapper().writeValueAsString(swaggerMap.get(schemaId));
        LOGGER.info(str);
        serviceCombClient.registerSchema(microserviceId, schemaId, str);
      } catch (RemoteOperationException e) {
        LOGGER.error("register swagger to server-center failed : {}", e.getMessage());
      } catch (JsonProcessingException e) {
        LOGGER.error("swagger parse failed : {}", e.getMessage());
      }
    });
  }

  private void registerSwaggerAsync(String microserviceId, List<String> schemaIds) {
    Executors.newSingleThreadExecutor().execute(() -> {
      registerSwaggerSync(microserviceId, schemaIds);
    });
  }

  /**
   * @param appName
   * @param serviceName
   * @param schemaId
   * @return
   */
  private String genXInterfaceName(String appName, String serviceName, String schemaId) {
    char[] intfName = schemaId.toCharArray();
    for (int i = 0; i < intfName.length; i++) {
      if (intfName[i] == '-' && intfName[i + 1] >= 'a' && intfName[i + 1] <= 'z') {
        intfName[i + 1] -= 32;
      }
    }
    return X_JAVA_INTERFACE_PREFIX + appName + "." + serviceName + "." + schemaId + "."
        + new String(intfName).replace("-", "") + INTF_SUFFIX;
  }


  /**
   * todo :real full class name , use aop modify source code, cache the class-name
   * springfox.documentation.swagger2.mappers.ModelMapper#modelsFromApiListings(com.google.common.collect.Multimap)
   * springfox.documentation.spring.web.scanners.ApiModelReader#read(springfox.documentation.spi.service.contexts.RequestMappingContext)
   * only can get method
   * @param defName
   * @return
   */
  private String genXDefinitionName(String defName) {
    return "";
  }

  /**
   * todo: use aop modify source code, cache the documentation-name -> class&interface
   * springfox.documentation.spring.web.scanners.ApiListingScanner.scan
   *
   * @param schemaId
   * @return
   */
  private String genClassName(String schemaId) {
    return "";
  }
}
