package com.mazalearn.scienceengine.experiments.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.AbstractScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.CurrentCoil;

public class CircuitActor extends Actor {
  private final IScience2DModel science2DModel;
  private ShapeRenderer shapeRenderer;
      
    
  public CircuitActor(IScience2DModel science2DModel) {
    super();
    this.science2DModel = science2DModel;
    this.shapeRenderer = new ShapeRenderer();
    this.setName("Circuit");
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    if (true) return;
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    // Draw the circuit
    shapeRenderer.begin(ShapeType.Line);
    shapeRenderer.setColor(Color.YELLOW);
    for (List<Science2DBody> circuit: science2DModel.getCircuits()) {
      if (circuit.size() <= 1) continue;
      for (int i = 1; i < circuit.size(); i++) {
        Science2DBody prev = circuit.get(i - 1);
        Science2DBody curr = circuit.get(i);
        //draw a line from prev body to this one
        shapeRenderer.line(prev.getPosition().x * ScienceEngine.PIXELS_PER_M, 
            prev.getPosition().y * ScienceEngine.PIXELS_PER_M, 
            curr.getPosition().x * ScienceEngine.PIXELS_PER_M, 
            curr.getPosition().y * ScienceEngine.PIXELS_PER_M);
      }
      Science2DBody prev = circuit.get(circuit.size() - 1);
      Science2DBody curr = circuit.get(0);
      // draw a line from last to first to close the circuit
      shapeRenderer.line(prev.getPosition().x * ScienceEngine.PIXELS_PER_M, 
          prev.getPosition().y * ScienceEngine.PIXELS_PER_M, 
          curr.getPosition().x * ScienceEngine.PIXELS_PER_M, 
          curr.getPosition().y * ScienceEngine.PIXELS_PER_M);
    }
    shapeRenderer.end();
  }
}