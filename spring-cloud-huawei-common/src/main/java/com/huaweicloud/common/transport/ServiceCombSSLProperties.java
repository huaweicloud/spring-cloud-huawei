package com.huaweicloud.common.transport;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author wangqijun
 * @Date 20:09 2019-09-03
 **/
@Component
@ConfigurationProperties("spring.cloud.servicecomb.credentials")
public class ServiceCombSSLProperties {
  private boolean enable = false;

  private String accessKey;

  private String secretKey;

  private String akskCustomCipher;

  private String project;

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getAkskCustomCipher() {
    return akskCustomCipher;
  }

  public void setAkskCustomCipher(String akskCustomCipher) {
    this.akskCustomCipher = akskCustomCipher;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }
}
