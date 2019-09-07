package org.springframework.cloud.servicecomb.discovery.client.model;

/**
 * @Author wangqijun
 * @Date 19:07 2019-09-05
 **/
public class SSLConfig {
  private boolean enable = false;

  private String accessKey;

  private String secretKey;

  private String akskCustomCipher = "default";

  private String project;

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

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
}
