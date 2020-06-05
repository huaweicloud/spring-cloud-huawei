package com.huaweicloud.common.schema;

import java.util.List;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/12/17
 **/
public interface ServiceCombSwaggerHandler {

  void init(String appName, String serviceName);

  void registerSwagger(String microserviceId, List<String> schemas);

  List<String> getSchemaIds();

  Map<String, String> getSchemasMap();

  Map<String, String> getSchemasSummaryMap();
}
