package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.AnimationAction;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.screens.StartScreen;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.view.IDoneCallback;

// doubts on direction
// Generate A at "random" point around magnet
// What is direction of field at A?
// doubts on shielding - not yet addressed
// TODO: Generate probe point within a rectangle and get field value from emField.
// Then prober becomes generalized.
class FieldDirectionProber extends AbstractProber {
  protected static final float TOLERANCE = 0.3f;
  private final Image image, userField, actualField;
  private final float x, y, width, height;  
  private Vector2 pos = new Vector2(), modelPos = new Vector2(), bField = new Vector2();
  private IExperimentModel model;
  
  public FieldDirectionProber(Skin skin, float x, float y, float width, float height, IExperimentModel model,
      final IDoneCallback doneCallback) {
    super();
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.model = model;
    
    userField = new Image(new TextureRegion(new Texture("images/fieldarrow.png")));
    userField.visible = false;
    userField.originX = 0;
    userField.originY = userField.height/2;
    
    actualField = new Image(new TextureRegion(new Texture("images/fieldarrow.png")));
    actualField.visible = true;
    actualField.originX = 0;
    actualField.originY = actualField.height/2;

    TextureRegion questionMark = 
        new TextureRegion(new Texture("images/questionmark.png"));
    image = new Image(questionMark) {
      Vector2 lastTouch = new Vector2(), current = new Vector2();
      @Override
      public boolean touchDown(float x, float y, int pointer) {
        lastTouch.set(x, y);
        userField.visible = true;
        userField.x = this.x + this.width/2;
        userField.y = this.y + this.height/3;
        actualField.x = this.x + this.width/2;
        actualField.y = this.y + this.height/3;
        actualField.rotation = bField.angle();
        return true;
      }
      
      @Override
      public void touchDragged(float x, float y, int pointer) {
        current.set(x, y);
        current.sub(lastTouch);
        userField.rotation = current.angle();
      }
      
      @Override
      public void touchUp(float x, float y, int pointer) {
        lastTouch.sub(x, y);
        float val = lastTouch.nor().dot(bField); // Should be -1
        final boolean success = Math.abs(val + 1) < TOLERANCE;
        actualField.visible = true;
        actualField.action(Sequence.$(Delay.$(2.5f),
            new AnimationAction() {
              @Override
              public void act(float delta) {
                doneCallback.done(success);
                done = true;
                actualField.visible = userField.visible = false;
              }
                  
              @Override
              public void setTarget(Actor actor) {}
    
              @Override
              public Action copy() {
                return null;
              }
            }));
      }
    };
    this.addActor(image);
    this.addActor(userField);
    this.addActor(actualField);
  }
  
  @Override
  public String getTitle() {
    return "Click and drag in direction of magnetic field";
  }
  
  private void generateProbePoint(Vector2 pos) {
    pos.set(MathUtils.random(2f) - 1, MathUtils.random(2f) - 1);
    pos.x *= width;
    pos.y *= height;
    pos.add(x + width/2, y + height/2);
  }

  private void getBFieldDirection(Vector2 viewPos) {
    modelPos.set(viewPos).mul(1f / ScienceEngine.PIXELS_PER_M);
    model.getBField(modelPos, bField /* output */);
    bField.nor();
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      generateProbePoint(pos);
      image.x = pos.x;
      image.y = pos.y;
      getBFieldDirection(pos);
    }
    this.visible = activate;
  }
}