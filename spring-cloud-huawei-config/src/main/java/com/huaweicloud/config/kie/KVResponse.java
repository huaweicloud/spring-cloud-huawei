package com.huaweicloud.config.kie;

import java.util.List;

/**
 * @Author GuoYl123
 * @Date 2020/1/7
 **/
public class KVResponse {

  private List<KVDoc> data;

  private LabelDocResponse label;

  private Integer num;

  private Integer size;

  private Integer total;

  public Integer getNum() {
    return num;
  }

  public void setNum(Integer num) {
    this.num = num;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public List<KVDoc> getData() {
    return data;
  }

  public LabelDocResponse getLabel() {
    return label;
  }

  public void setData(List<KVDoc> data) {
    this.data = data;
  }

  public void setLabel(LabelDocResponse label) {
    this.label = label;
  }
}
