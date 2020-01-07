package com.huaweicloud.config.kie;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2020/1/7
 **/
public class KVDoc {

  @JsonAlias("_id")
  private String id;

  private String check;

  private String domain;

  private String key;

  @JsonAlias("label_id")
  private String labelId;

  private Map<String, String> labels = new HashMap<String, String>();

  private Integer revision;

  private String value;

  @JsonAlias("value_type")
  private String valueType;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getCheck() {
    return check;
  }

  public String getDomain() {
    return domain;
  }

  public String getLabelId() {
    return labelId;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public Integer getRevision() {
    return revision;
  }

  public String getValue() {
    return value;
  }

  public void setCheck(String check) {
    this.check = check;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setLabelId(String labelId) {
    this.labelId = labelId;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public void setRevision(Integer revision) {
    this.revision = revision;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValueType() {
    return valueType;
  }
}
