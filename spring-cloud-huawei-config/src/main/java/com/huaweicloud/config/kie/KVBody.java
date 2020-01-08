package com.huaweicloud.config.kie;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.huaweicloud.config.ServiceCombConfigProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2020/1/8
 **/
public class KVBody {

  private Map<String, String> labels = new HashMap<String, String>();

  private String value;

  @JsonAlias("value_type")
  private String valueType;

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public void initLabels(ServiceCombConfigProperties serviceCombConfigProperties) {
    labels.put("env", serviceCombConfigProperties.getEnv());
    labels.put("app", serviceCombConfigProperties.getAppName());
    labels.put("service", serviceCombConfigProperties.getServiceName());
    labels.put("version", serviceCombConfigProperties.getVersion());
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }
}
