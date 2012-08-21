// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

/**
 * SourceCoil is the model of the source coil used in an electromagnet.
 * 
 * @author sridhar
 */
public class SourceCoil extends AbstractCoil {

  public SourceCoil() {
    super();

    // pack the loops close together
    setLoopSpacing(getWireWidth());
  }

  /**
   * If the wire width is changed, also change the loop spacing so that the
   * loops remain packed close together.
   * 
   * @param wireWidth
   */
  public void setWireWidth(double wireWidth) {
    super.setWireWidth(wireWidth);
    setLoopSpacing(getWireWidth());
  }
}
