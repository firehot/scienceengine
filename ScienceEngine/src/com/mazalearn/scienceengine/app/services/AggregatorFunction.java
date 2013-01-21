package com.mazalearn.scienceengine.app.services;

import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.lang.IFunction;

// Currently all functions take exactly one argument and produce float result
public enum AggregatorFunction implements IFunction.A1 {
  Min(new Aggregator() {
    float min;
    public void init() { min = 0; }
    public void visit(float value) { min = Math.min(min, value); }
    public float getValue() { return min;}
  }),
  Max(new Aggregator() {
    float max;
    public void init() { max = 0; }
    public void visit(float value) { max = Math.max(max, value); }
    public float getValue() { return max;}
  }),
  Count(new Aggregator() {
    float count;
    public void init() { count = 0; }
    public void visit(float value) { count++; }
    public float getValue() { return count;}
  });

  interface Aggregator {
    void init();
    void visit(float value);
    float getValue();
  }
  
  private Aggregator aggregator;

  private AggregatorFunction(Aggregator aggregator) {
    this.aggregator = aggregator;
  }
  
  @Override
  public float eval(String parameter) {
    return ScienceEngine.getEventLog().eval(aggregator, parameter);
  }
}
