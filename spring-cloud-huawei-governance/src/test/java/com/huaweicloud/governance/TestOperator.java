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
package com.huaweicloud.governance;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.governance.marker.operator.CompareOperator;
import org.apache.servicecomb.governance.marker.operator.ContainsOperator;
import org.apache.servicecomb.governance.marker.operator.ExactOperator;
import org.apache.servicecomb.governance.marker.operator.MatchOperator;
import org.apache.servicecomb.governance.marker.operator.PrefixOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestOperator {

  private final Map<String, MatchOperator> operatorMap;

  private static final String PREFIX_KEY = "prefix";

  private static final String EXACT_KEY = "exact";

  private static final String CONTAINS_KEY = "contains";

  private static final String COMPARE_KEY = "compare";

  {
    operatorMap = new HashMap<>();
    operatorMap.put(PREFIX_KEY, new PrefixOperator());
    operatorMap.put(EXACT_KEY, new ExactOperator());
    operatorMap.put(CONTAINS_KEY, new ContainsOperator());
    operatorMap.put(COMPARE_KEY, new CompareOperator());
  }

  @Test
  public void testPrefix() {
    String patternStr = "/xx";
    String targetStr1 = "/xxxx/xxx1";
    String targetStr2 = "xxx";
    Assertions.assertTrue(operatorMap.get(PREFIX_KEY).match(targetStr1, patternStr));
    Assertions.assertFalse(operatorMap.get(PREFIX_KEY).match(targetStr2, patternStr));
  }

  @Test
  public void testContain() {
    String patternStr = "/xxx";
    String targetStr1 = "/xxxx/xxx1";
    String targetStr2 = "/12344";
    Assertions.assertTrue(operatorMap.get(CONTAINS_KEY).match(targetStr1, patternStr));
    Assertions.assertFalse(operatorMap.get(CONTAINS_KEY).match(targetStr2, patternStr));
  }

  @Test
  public void testCompare() {
    String patternStr = ">123";
    String targetStr1 = "133";
    String targetStr2 = "90";
    Assertions.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr1, patternStr));
    Assertions.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr2, patternStr));

    String patternStr2 = ">=123";
    String targetStr3 = "123";
    Assertions.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr3, patternStr2));
    Assertions.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr2, patternStr2));

    String patternStr3 = "=123";
    Assertions.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr3, patternStr3));
    Assertions.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr2, patternStr3));

    String patternStr4 = "!123";
    Assertions.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr3, patternStr4));
    Assertions.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr2, patternStr4));

    String patternStr5 = "<=-123";
    String targetStr4 = "-123";
    Assertions.assertFalse(operatorMap.get(COMPARE_KEY).match(targetStr3, patternStr5));
    Assertions.assertTrue(operatorMap.get(COMPARE_KEY).match(targetStr4, patternStr5));
  }

  @Test
  public void testExact() {
    String patternStr = "/xxx";
    String targetStr1 = "/xxx";
    String targetStr2 = "/12344";
    Assertions.assertTrue(operatorMap.get(EXACT_KEY).match(targetStr1, patternStr));
    Assertions.assertFalse(operatorMap.get(EXACT_KEY).match(targetStr2, patternStr));
  }
}
