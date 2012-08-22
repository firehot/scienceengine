// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.model.electromagnetism;

/**
 * Battery is the model of a DC battery.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class Battery extends AbstractCurrentSource {
    
    public Battery() {
        super();
    }
    
    @Override
    public String getName() {
      return "Battery";
    }

}