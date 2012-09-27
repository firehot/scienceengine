package com.mazalearn.scienceengine.experiments.electromagnetism.probe;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.AnimationAction;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.probe.IDoneCallback;
import com.mazalearn.scienceengine.core.probe.ProbeImage;

// doubts on direction
// Generate A at "random" point around active elements.
// What is direction of field at A?
// doubts on shielding - not yet addressed
public class FieldDirectionProber extends AbstractFieldProber {
  protected static final float TOLERANCE = 0.3f;
  private final Image image, userField;
  private Vector2[] pos, bField;
  
  public FieldDirectionProber(IExperimentModel model,
      final IDoneCallback doneCallback, List<Actor> actors, Actor dashboard) {
    super(model, actors, dashboard);
    
    this.pos = new Vector2[] { new Vector2()};
    this.bField = new Vector2[] { new Vector2()};
    
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
        float val = lastTouch.nor().dot(bField[0]); // Should be -1
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
      generateProbePoints(pos, bField);
      image.x = pos[0].x - image.width/2;
      image.y = pos[0].y - image.height/2;
      bField[0].nor();
    }
    this.visible = activate;
  }

  @Override
  protected boolean arePointsAcceptable(Vector2[] points) {
    return true;
  }
}