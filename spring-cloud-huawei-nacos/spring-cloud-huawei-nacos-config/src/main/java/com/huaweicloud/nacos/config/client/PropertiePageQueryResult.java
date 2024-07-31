/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.nacos.config.client;

import java.util.List;

public class PropertiePageQueryResult {
  private Integer totalCount;

  private Integer pageNumber;

  private Integer pagesAvailable;

  private List<PropertyConfigItem> pageItems;

  public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public Integer getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber) {
    this.pageNumber = pageNumber;
  }

  public Integer getPagesAvailable() {
    return pagesAvailable;
  }

  public void setPagesAvailable(Integer pagesAvailable) {
    this.pagesAvailable = pagesAvailable;
  }

  public List<PropertyConfigItem> getPageItems() {
    return pageItems;
  }

  public void setPageItems(List<PropertyConfigItem> pageItems) {
    this.pageItems = pageItems;
  }
}
