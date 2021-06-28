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

package com.huaweicloud.samples;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cse.v1.test")
public class ConsumerConfigurationProperties {
  private String foo;

  private String bar;

  private List<String> sequences;

  private List<ConfigModel> configModels;

  public String getFoo() {
    return foo;
  }

  public void setFoo(String foo) {
    this.foo = foo;
  }

  public String getBar() {
    return bar;
  }

  public void setBar(String bar) {
    this.bar = bar;
  }

  public List<String> getSequences() {
    return sequences;
  }

  public void setSequences(List<String> sequences) {
    this.sequences = sequences;
  }

  public List<ConfigModel> getConfigModels() {
    return configModels;
  }

  public void setConfigModels(List<ConfigModel> configModels) {
    this.configModels = configModels;
  }
}
