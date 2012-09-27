package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.AnimationAction;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.view.IDoneCallback;
import com.mazalearn.scienceengine.core.view.ProbeImage;

// doubts on direction
// Generate A at "random" point around magnet
// What is direction of field at A?
// doubts on shielding - not yet addressed
// TODO: Generate probe point within a rectangle and get field value from emField.
// Then prober becomes generalized.
class FieldDirectionProber extends AbstractProber {
  protected static final float TOLERANCE = 0.3f;
  private final Image image, userField;
  private Vector2 pos = new Vector2(), bField = new Vector2();
  
  public FieldDirectionProber(IExperimentModel model,
      final IDoneCallback doneCallback, List<Actor> actors, Actor dashboard) {
    super(model, actors, dashboard);
    
    userField = new Image(new TextureRegion(new Texture("images/fieldarrow-yellow.png")));
    userField.visible = false;
    userField.originX = 0;
    userField.originY = userField.height/2;
    
    image = new ProbeImage() {
      Vector2 lastTouch = new Vector2(), current = new Vector2();
      @Override
      public boolean touchDown(float x, float y, int pointer) {
        lastTouch.set(x, y);
        userField.visible = true;
        userField.x = this.x + this.width/2;
        userField.y = this.y + this.height/3;
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
        fieldMeterActor.visible = true;
        userField.action(Sequence.$(Delay.$(2f),
            new AnimationAction() {
              @Override
              public void act(float delta) {
                doneCallback.done(success);
                done = true;
                fieldMeterActor.visible = userField.visible = false;
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
  }
  
  @Override
  public String getTitle() {
    return "Click and drag in direction of magnetic field";
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      generateProbePoints(pos);
      image.x = pos.x - image.width/2;
      image.y = pos.y - image.height/2;
      getBField(pos, bField);
      fieldMeter.resetInitial();
      pos.mul(1f/ScienceEngine.PIXELS_PER_M);
      fieldMeter.addFieldSample(pos.x, pos.y, bField.angle() * MathUtils.degreesToRadians, bField.len());
      bField.nor();
    }
    this.visible = activate;
  }

  @Override
  protected boolean arePointsAcceptable(Vector2[] points) {
    return true;
  }
}