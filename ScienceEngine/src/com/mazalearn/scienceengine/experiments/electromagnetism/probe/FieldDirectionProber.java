package com.mazalearn.scienceengine.experiments.electromagnetism.probe;

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
import com.mazalearn.scienceengine.core.probe.ProbeImage;
import com.mazalearn.scienceengine.core.probe.ProbeManager;

// doubts on direction
// Generate A at "random" point around active elements.
// What is direction of field at A?
// doubts on shielding - not yet addressed
public class FieldDirectionProber extends AbstractFieldProber {
  private final Image image, userField;
  private Vector2[] points, bFields;
  
  public FieldDirectionProber(IExperimentModel model, final ProbeManager probeManager) {
    super(model, probeManager);
    
    this.points = new Vector2[] { new Vector2()};
    this.bFields = new Vector2[] { new Vector2()};
    
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
        float val = lastTouch.nor().dot(bFields[0]); // Should be -1
        final boolean success = Math.abs(val + 1) < TOLERANCE;
        fieldMeterActor.visible = true;
        userField.action(Sequence.$(Delay.$(2f),
            new AnimationAction() {
              @Override
              public void act(float delta) {
                probeManager.done(success);
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
      probeManager.randomizeConfig();
      generateProbePoints(points);
      getBField(points[0], bFields[0]);
      createFieldMeterSamples(points, bFields);
      image.x = points[0].x - image.width/2;
      image.y = points[0].y - image.height/2;
      bFields[0].nor();
    }
    this.visible = activate;
  }
}