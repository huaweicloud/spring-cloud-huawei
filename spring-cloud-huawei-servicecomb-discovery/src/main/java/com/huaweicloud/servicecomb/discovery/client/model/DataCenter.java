package com.huaweicloud.servicecomb.discovery.client.model;

/**
 * @Author GuoYl123
 * @Date 2020/3/31
 **/
public class DataCenter {

  private String name;

  private String region;

  private String availableZone;

  public String getZone() {
    return name + region + availableZone;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getAvailableZone() {
    return availableZone;
  }

  public void setAvailableZone(String availableZone) {
    this.availableZone = availableZone;
  }
}
