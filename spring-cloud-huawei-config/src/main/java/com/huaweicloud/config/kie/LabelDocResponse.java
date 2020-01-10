package com.huaweicloud.config.kie;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2020/1/7
 **/
public class LabelDocResponse {

  @JsonAlias("label_id")
  private String labelId;

  private Map<String, String> labels = new HashMap<String, String>();

  public String getLabelId() {
    return labelId;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabelId(String labelId) {
    this.labelId = labelId;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }
}
