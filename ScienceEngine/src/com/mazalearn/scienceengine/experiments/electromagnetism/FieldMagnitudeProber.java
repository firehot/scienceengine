package com.mazalearn.scienceengine.experiments.electromagnetism;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.AnimationAction;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.view.IDoneCallback;
import com.mazalearn.scienceengine.core.view.ProbeImage;

// doubts on magnitude
// Generate A, B at two "random" points around magnet
// Is the field stronger at A or B?
class FieldMagnitudeProber extends AbstractProber {
  
  private final class ClickResult implements ClickListener {
    private final IDoneCallback doneCallback;
    private final boolean success;
 
    private ClickResult(boolean success, IDoneCallback doneCallback) {
      this.success = success;
      this.doneCallback = doneCallback;
    }

    @Override
    public void click(Actor actor, float x, float y) {
      fieldMeterActor.visible = true;
      actor.action(Sequence.$(Delay.$(2f),
          new AnimationAction() {
            @Override
            public void act(float delta) {
              fieldMeterActor.visible = false;
              doneCallback.done(success);
              done = true;
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

  private final Image imageCorrect, imageWrong;
  // Temporary vectors
  private Vector2 pos0 = new Vector2(), pos1 = new Vector2();
  private Vector2 bField0 = new Vector2(), bField1 = new Vector2();
    
  public FieldMagnitudeProber(IExperimentModel model,
      final IDoneCallback doneCallback, List<Actor> actors, Actor dashboard) {
    super(model, actors, dashboard);
    imageCorrect = new ProbeImage();
    imageCorrect.setClickListener(new ClickResult(true, doneCallback));
    imageWrong = new ProbeImage();
    imageWrong.setClickListener(new ClickResult(false, doneCallback));
    this.addActor(imageCorrect);
    this.addActor(imageWrong);
    fieldMeterActor.visible = false;
  }
  
  @Override
  public String getTitle() {
    return "Click where the magnetic field is stronger";
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      // Generate two random points P1, P2 in unit circle.
      // If P0.r ~ P1.r AND (P0.x ~ P1.x) OR (P0.y ~ P1.y) try again
      // Scale P0.x, P1.x by magnet width*2 and P0.y, P1.y by magnet height*2
      generateProbePoints(pos0, pos1);
      
      if (bField0.len() > bField1.len()) {
        imageCorrect.x = pos0.x - imageCorrect.width/2;
        imageCorrect.y = pos0.y - imageCorrect.height/2;
        imageWrong.x = pos1.x - imageWrong.width/2;
        imageWrong.y = pos1.y - imageWrong.width/2;
      } else {
        imageCorrect.x = pos1.x - imageCorrect.width/2;
        imageCorrect.y = pos1.y - imageCorrect.height/2;
        imageWrong.x = pos0.x - imageWrong.width/2;
        imageWrong.y = pos0.y - imageWrong.width/2;
      }
      fieldMeter.resetInitial();
      pos0.mul(1f/ScienceEngine.PIXELS_PER_M);
      pos1.mul(1f/ScienceEngine.PIXELS_PER_M);
      fieldMeter.addFieldSample(pos0.x, pos0.y, bField0.angle() * MathUtils.degreesToRadians, bField0.len());
      fieldMeter.addFieldSample(pos1.x, pos1.y, bField1.angle() * MathUtils.degreesToRadians, bField1.len());
    }
    this.visible = activate;
  }

  protected boolean arePointsAcceptable(Vector2[] points) {
    getBField(points[0], bField0);
    getBField(points[1], bField1);
    if (haveSimilarMagnitudes(bField0.len(), bField1.len())) return false;
    return true;
  }
}