package com.mazalearn.scienceengine.core.lang;

public interface IFunction {
  public interface A0 {
    public float eval();
  }
  public interface A1 {
    public float eval(String argName);
  }
  public interface A2 {
    public float eval(String argName1, String argName2);
  }
}