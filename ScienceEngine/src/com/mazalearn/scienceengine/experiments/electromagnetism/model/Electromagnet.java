// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

/**
 * Electromagnet is the model of an electromagnet. It is derived from the
 * CoilMagnet model.
 * 
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class Electromagnet extends CoilMagnet {

  // ----------------------------------------------------------------------------
  // Instance data
  // ----------------------------------------------------------------------------

  private SourceCoil sourceCoilModel;
  private AbstractCurrentSource currentSource;
  private boolean isFlipped;
  public static final int ELECTROMAGNET_LOOPS_MAX = 4;

  // ----------------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------------

  /**
   * Sole constructor.
   * 
   * @param sourceCoilModel
   *          the electromagnet's coil
   * @param currentSource
   *          the electromagnet's current source
   */
  public Electromagnet(EMField emField, SourceCoil sourceCoilModel,
      AbstractCurrentSource currentSource) {
    super(emField);
    assert (sourceCoilModel != null);
    assert (currentSource != null);

    this.sourceCoilModel = sourceCoilModel;
    this.currentSource = currentSource;
    this.isFlipped = false;

    update();
  }

  @Override
  public String getName() {
    return "Electromagnet";
  }

  /**
   * Sets the electromagnet's current source.
   * 
   * @param currentSource
   */
  public void setCurrentSource(AbstractCurrentSource currentSource) {
    assert (currentSource != null);
    this.currentSource = currentSource;
    update();
  }

  /**
   * Gets the eletromagnet's current source.
   * 
   * @return the current source
   */
  public AbstractCurrentSource getCurrentSource() {
    return this.currentSource;
  }

  /*
   * Updates current in the coil and strength of the magnet.
   */
  public void update() {

    /*
     * The magnet size is a circle that has the same radius as the coil. Adding
     * half the wire width makes it look a little better.
     */
    double diameter = (2 * this.sourceCoilModel.getRadius())
        + (this.sourceCoilModel.getWireWidth() / 2);
    super.setSize((float) diameter, (float) diameter);

    // Current amplitude is proportional to amplitude of the current source.
    this.sourceCoilModel.setCurrentAmplitude(this.currentSource.getAmplitude());

    // Compute the electromagnet's emf amplitude.
    float amplitude = (this.sourceCoilModel.getNumberOfLoops() / (float) ELECTROMAGNET_LOOPS_MAX)
        * this.currentSource.getAmplitude();
    amplitude = Clamp.clamp(-1f, amplitude, 1f);

    // Flip the polarity
    if (amplitude >= 0 && this.isFlipped) {
      flipPolarity();
      this.isFlipped = false;
    } else if (amplitude < 0 && !this.isFlipped) {
      flipPolarity();
      this.isFlipped = true;
    }

    /*
     * Set the strength. This is a bit of a "fudge". We set the strength of the
     * magnet to be proportional to its emf.
     */
    double strength = Math.abs(amplitude) * getMaxStrength();
    setStrength(strength);
  }
}