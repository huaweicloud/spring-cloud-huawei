package com.huaweicloud.sample;

/**
 * @Author GuoYl123
 * @Date 2020/4/9
 **/
public class Info {

  String var1 = "init";

  int var2 = 1;

  MinorInfo var3 = new MinorInfo();

  public String getVar1() {
    return var1;
  }

  public void setVar1(String var1) {
    this.var1 = var1;
  }

  public int getVar2() {
    return var2;
  }

  public void setVar2(int var2) {
    this.var2 = var2;
  }

  public MinorInfo getVar3() {
    return var3;
  }

  public void setVar3(MinorInfo var3) {
    this.var3 = var3;
  }

  static class MinorInfo {

    String info = "message info!";

    boolean dummy = true;

    public boolean isDummy() {
      return dummy;
    }

    public void setDummy(boolean dummy) {
      this.dummy = dummy;
    }

    public String getInfo() {
      return info;
    }

    public void setInfo(String info) {
      this.info = info;
    }
  }
}
