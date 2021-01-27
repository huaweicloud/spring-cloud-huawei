/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huaweicloud.sample;

/**
 * @Author GuoYl123
 * @Date 2020/4/9
 **/
public class Info {

  String var1;

  int var2;

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

    String info;

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
