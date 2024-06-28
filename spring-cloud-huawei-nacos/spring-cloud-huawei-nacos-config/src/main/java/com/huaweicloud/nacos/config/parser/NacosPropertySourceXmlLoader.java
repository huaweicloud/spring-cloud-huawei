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

package com.huaweicloud.nacos.config.parser;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.huaweicloud.nacos.config.NacosConfigConst;

public class NacosPropertySourceXmlLoader extends AbstactNacosPropertySourceLoader implements Ordered {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosPropertySourceXmlLoader.class);

  @Override
  public String[] getFileExtensions() {
    return new String[] { "xml" };
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }

  @Override
  protected List<PropertySource<?>> doLoad(String name, Resource resource) throws IOException {
    Map<String, Object> propertySourceMap = parseXmlContext2Map(resource);
    return Collections.singletonList(new OriginTrackedMapPropertySource(name, propertySourceMap, true));
  }

  private Map<String, Object> parseXmlContext2Map(Resource resource) throws IOException {
    Map<String, Object> map = new LinkedHashMap<>();
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(resource.getInputStream());
      if (document == null) {
        return Collections.emptyMap();
      }
      parseNodesToMap(document.getChildNodes(), map, "");
    } catch (ParserConfigurationException | SAXException e) {
      LOGGER.error("xml context parse to map error", e);
      throw new IOException(e);
    }
    return map;
  }

  private void parseNodesToMap(NodeList childNodes, Map<String, Object> map, String tempKey) {
    if (childNodes == null || childNodes.getLength() < 1) {
      return;
    }
    String rebuildKey = tempKey == null ? "" : tempKey;
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      String value = node.getNodeValue();
      value = value == null ? "" : value.trim();
      if (StringUtils.isEmpty(node.getNodeName())) {
        continue;
      }
      String currentKey = StringUtils.isEmpty(rebuildKey) ? node.getNodeName()
          : rebuildKey + NacosConfigConst.DOT + node.getNodeName();
      parseNodeAttributes(node.getAttributes(), map, currentKey);
      if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
        parseNodesToMap(node.getChildNodes(), map, currentKey);
        continue;
      }
      if (value.isEmpty()) {
        continue;
      }
      map.put(rebuildKey, value);
    }
  }

  private void parseNodeAttributes(NamedNodeMap attributes, Map<String, Object> map, String currentKey) {
    if (attributes == null || attributes.getLength() < 1) {
      return;
    }
    for (int i = 0; i < attributes.getLength(); i++) {
      Node node = attributes.item(i);
      if (node == null) {
        continue;
      }
      if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
        if (StringUtils.isEmpty(node.getNodeName()) || StringUtils.isEmpty(node.getNodeValue())) {
          continue;
        }
        map.put(String.join(NacosConfigConst.DOT, currentKey, node.getNodeName()), node.getNodeValue());
      }
    }
  }
}
