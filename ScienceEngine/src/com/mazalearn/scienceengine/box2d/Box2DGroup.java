package com.mazalearn.scienceengine.box2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Envelops a Box2D World which is modeled as a Scene2D Group.
 * All box2D actors should be added to this group.
 * They will then be advanced.
 * @author sridhar
 *
 */
public class Box2DGroup extends Group {
  public World box2DWorld;
  
  public Box2DGroup(float width, float height) {
    // Initialize the world for Box2D if not already available
    Vector2 gravity = new Vector2(0.0f, 0.0f);
    boolean doSleep = true;
    box2DWorld = new World(gravity, doSleep);
  }

  /**
   * Draw and advance Box2D World
   * 
   */
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);    
    box2DWorld.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
  }
}
