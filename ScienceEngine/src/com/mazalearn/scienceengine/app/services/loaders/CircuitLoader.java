package com.mazalearn.scienceengine.app.services.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;

public class CircuitLoader {

  static void loadCircuits(Array<?> circuits, IScience2DModel science2DModel) {
    science2DModel.removeCircuits();
  
    if (circuits == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading circuits");
    
    for (int i = 0; i < circuits.size; i++) {
      @SuppressWarnings("unchecked")
      Array<String> circuit = (Array<String>) circuits.get(i);
      CircuitLoader.loadCircuit(circuit, science2DModel);
    }
  }

  private static void loadCircuit(Array<String> circuit, IScience2DModel science2DModel) {
    Gdx.app.log(ScienceEngine.LOG, "Loading circuit");
    CircuitElement[] circuitElements = new CircuitElement[circuit.size];
    for (int i = 0; i < circuit.size; i++) {
      String name = circuit.get(i);
      Science2DBody body = science2DModel.findBody(name);
      if (body == null) {
        throw new IllegalArgumentException("Body not found: " + name);
      }
      circuitElements[i] = (CircuitElement) body;      
    }
    science2DModel.addCircuit(circuitElements);
  }

}
