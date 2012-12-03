package com.mazalearn.scienceengine.domains.electromagnetism.view;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.Parameter;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.domains.electromagnetism.model.CurrentCoil;
import com.mazalearn.scienceengine.domains.electromagnetism.model.BarMagnet.Mode;

public class CurrentCoilActor extends Science2DActor {
  private final CurrentCoil currentCoil;
  private static TextureRegion commutatorNone = 
      new TextureRegion(new Texture("images/currentcoil_nocommutator.png"));
  private static TextureRegion commutatorAc = 
      new TextureRegion(new Texture("images/currentcoil_accommutator.png"));
  private static TextureRegion commutatorDc = 
      new TextureRegion(new Texture("images/currentcoil_dccommutator.png"));
  private Vector2 newPos = new Vector2();
    
    
  public CurrentCoilActor(Science2DBody body) {
    super(body, commutatorNone);
    this.currentCoil = (CurrentCoil) body;
    this.setAllowMove(false);
    // Rotation listener
    this.addListener(new ClickListener() {
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        // Get negative of movement vector
        lastTouch.sub(event.getStageX(), event.getStageY());
        // Scale displacement vector suitably to get a proportional force
        lastTouch.mul(-5);
        // view coords of current touch
        newPos.set(event.getStageX(), event.getStageY());
        // box2d point of current touch
        getBox2DPositionFromViewPosition(newPos, newPos, getRotation());
        // Use center as origin - dont understand why this step
        newPos.sub(currentCoil.getWidth()/2, currentCoil.getHeight()/2);
        currentCoil.applyForce(lastTouch, newPos);
        ScienceEngine.selectParameter(Parameter.Rotate, (IScience2DStage) getStage());
      }
    });

  }
  
  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Flip if negative current so that +, - on coil are correctly shown
    TextureRegion textureRegion = null;
    float rotation = getRotation();
    rotation += currentCoil.getCurrent() < 0 ? 180 : 0;
    switch(currentCoil.getCommutatorType()) {
    case Commutator: 
      textureRegion = commutatorDc;
      break;
    case Connector:
      textureRegion = commutatorAc;
      break;
    case Disconnected:
      textureRegion = commutatorNone;
    }
    batch.draw(textureRegion, getX(), getY(), this.getOriginX(), 
        this.getOriginY(), getWidth(), getHeight(), 1, 1, rotation);
  }
}