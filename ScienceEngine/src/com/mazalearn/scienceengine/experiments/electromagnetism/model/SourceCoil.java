// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

/**
 * SourceCoil is the model of the source coil used in an electromagnet.
 * 
 * @author sridhar
 */
public class SourceCoil extends AbstractCoil {

  public SourceCoil() {
    super("SourceCoil");

    // pack the loops close together
    setLoopSpacing(getWireWidth());
  }

  @Override
  public String getName() {
    return "SourceCoil";
  }

  /**
   * If the wire width is changed, also change the loop spacing so that the
   * loops remain packed close together.
   * 
   * @param wireWidth
   */
  public void setWireWidth(float wireWidth) {
    super.setWireWidth(wireWidth);
    setLoopSpacing(getWireWidth());
  }
}
