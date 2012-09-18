// Copyright 2002-2011, University of Colorado

package com.mazalearn.scienceengine.experiments.electromagnetism.model;

/**
 * Battery is the model of a DC battery.
 *
 * @author Chris Malley (cmalley@pixelzoom.com)
 */
public class Battery extends AbstractCurrentSource {
    
    public Battery(String name, float x, float y, float angle) {
        super(ComponentType.Battery, name, x, y, angle);
    }
}