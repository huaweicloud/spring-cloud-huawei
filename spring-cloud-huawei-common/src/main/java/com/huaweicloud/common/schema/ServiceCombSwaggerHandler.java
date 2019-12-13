package com.huaweicloud.common.schema;

import java.util.List;

/**
 * @Author GuoYl123
 * @Date 2019/12/17
 **/
public interface ServiceCombSwaggerHandler {

  void init();

  void registerSwagger(String microserviceId, List<String> schemas);

  List<String> getSchemas();
}
