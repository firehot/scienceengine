package com.mazalearn.scienceengine.domains.electromagnetism.view;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.ICurrent.CircuitElement;

public class CircuitActor extends Actor {
  public static final String COMPONENT_TYPE = "CircuitElement";
  private static final int WIRE_WIDTH = 4;
  private final List<CircuitElement> circuit;
  private ShapeRenderer shapeRenderer;
  private Vector2 delta = new Vector2();
      
    
  public CircuitActor(List<CircuitElement> circuit) {
    super();
    this.circuit = circuit;
    this.shapeRenderer = new ShapeRenderer();
    this.setName(COMPONENT_TYPE);
  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    if (circuit.size() <= 1) return;
    batch.end();
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    // Draw the circuit
    shapeRenderer.begin(ShapeType.FilledRectangle);
    shapeRenderer.setColor(Color.GREEN);
    for (int i = 1; i < circuit.size(); i++) {
      CircuitElement prev = circuit.get(i - 1);
      CircuitElement curr = circuit.get(i);
      if (!prev.isActive() || !curr.isActive()) continue;
      //draw a line from prev body to this one
      drawConnection(prev.getT2Position(), curr.getT1Position());
    }
    CircuitElement prev = circuit.get(circuit.size() - 1);
    CircuitElement curr = circuit.get(0);
    if (prev.isActive() && curr.isActive()) {
      drawConnection(prev.getT2Position(), curr.getT1Position());
    }
    shapeRenderer.end();
    batch.begin();
  }

  private void drawConnection(Vector2 from, Vector2 to) {
    if (from.x == Float.NaN || from.y == Float.NaN || to.x == Float.NaN || to.y == Float.NaN) {
      return;
    }
    delta.set(to).sub(from).mul(ScienceEngine.PIXELS_PER_M);
    shapeRenderer.identity();
    shapeRenderer.translate(from.x * ScienceEngine.PIXELS_PER_M, from.y * ScienceEngine.PIXELS_PER_M, 0);
    // We will do Manhattan wiring covering shorter of deltax and deltay first, then the other
    if (Math.abs(delta.x) >= Math.abs(delta.y)) {
      shapeRenderer.rotate(0, 0, 1, delta.x < 0 ? 180 : 0);
      shapeRenderer.filledRect(0, -WIRE_WIDTH/2, Math.abs(delta.x), WIRE_WIDTH);
      shapeRenderer.translate(Math.abs(delta.x), 0, 0);
      shapeRenderer.rotate(0, 0, 1, delta.x < 0 ? -180 : 0);
      shapeRenderer.rotate(0, 0, 1, delta.y < 0 ? 180 : 0);
      shapeRenderer.filledRect(-WIRE_WIDTH/2, 0, WIRE_WIDTH, Math.abs(delta.y));      
    } else {
      shapeRenderer.rotate(0, 0, 1, delta.y < 0 ? 180 : 0);
      shapeRenderer.filledRect(-WIRE_WIDTH/2, 0, WIRE_WIDTH, Math.abs(delta.y));
      shapeRenderer.translate(0, Math.abs(delta.y), 0);
      shapeRenderer.rotate(0, 0, 1, delta.y < 0 ? -180 : 0);
      shapeRenderer.rotate(0, 0, 1, delta.x < 0 ? 180 : 0);
      shapeRenderer.filledRect(0, -WIRE_WIDTH/2, Math.abs(delta.x), WIRE_WIDTH);      
    }
    /*shapeRenderer.rotate(0, 0, 1, delta.angle());
    shapeRenderer.filledRect(0, 0, delta.len() * ScienceEngine.PIXELS_PER_M, WIRE_WIDTH); */
  }
}