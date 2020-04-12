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

package com.huaweicloud.common.transport;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;

/**
 * @Author wangqijun
 * @Date 11:24 2019-07-08
 **/
public class Response {
  private int statusCode;

  private String statusMessage;

  private String content;

  private Map<String, String> headers = new HashMap<>();

  public String getHeader(String key) {
    return headers.get(key);
  }

  public void setHeaders(Header[] headers) {
    for (Header header : headers) {
      this.headers.put(header.getName(), header.getValue());
    }
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "Response{" +
        "statusCode=" + statusCode +
        ", statusMessage='" + statusMessage + '\'' +
        ", content='" + content + '\'' +
        ", headers=" + headers +
        '}';
  }
}
