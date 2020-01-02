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
 * @Date 2019/12/16
 **/
public class Foo {
  String field1;
  int field2;
  Foo foo;

  public Foo() {
  }

  public Foo(String field1, int field2, Foo foo) {
    this.field1 = field1;
    this.field2 = field2;
    this.foo = foo;
  }

  public String getField1() {
    return field1;
  }

  public void setField1(String field1) {
    this.field1 = field1;
  }

  public int getField2() {
    return field2;
  }

  public void setField2(int field2) {
    this.field2 = field2;
  }

  public Foo getFoo() {
    return foo;
  }

  public void setFoo(Foo foo) {
    this.foo = foo;
  }

  @Override
  public String toString() {
    return "Foo{" +
        "field1='" + field1 + '\'' +
        ", field2=" + field2 +
        ", foo=" + foo +
        '}';
  }
}
