package com.huaweicloud.servicecomb.discovery.client.model;

import java.util.List;

/**
 * @Author GuoYl123
 * @Date 2020/5/9
 **/
public class SchemaResponse {

  List<SchemaRequest> schemas;

  public List<SchemaRequest> getSchemas() {
    return schemas;
  }

  public void setSchemas(
      List<SchemaRequest> schemas) {
    this.schemas = schemas;
  }
}
