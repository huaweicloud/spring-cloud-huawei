package com.huaweicloud.governance.policy;

public abstract class AbstractPolicy implements Policy {

  private String name;

  private String match;

  public String getMatch() {
    return match;
  }

  public void setMatch(String match) {
    this.match = match;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean match(String str) {
    return str.startsWith(match);
  }

  @Override
  public String name() {
    return name;
  }
}
