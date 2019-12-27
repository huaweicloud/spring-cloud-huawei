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
