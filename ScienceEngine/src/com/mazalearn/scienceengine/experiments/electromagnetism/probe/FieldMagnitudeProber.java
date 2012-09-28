package com.mazalearn.scienceengine.experiments.electromagnetism.probe;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.AnimationAction;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.mazalearn.scienceengine.core.model.IExperimentModel;
import com.mazalearn.scienceengine.core.probe.IDoneCallback;
import com.mazalearn.scienceengine.core.probe.ProbeImage;
import com.mazalearn.scienceengine.core.probe.ProbeManager;

// doubts on magnitude
// Generate A, B at two "random" points around magnet
// Is the field stronger at A or B?
public class FieldMagnitudeProber extends AbstractFieldProber {
  
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
  private Vector2[] points;
  private Vector2[] bFields;
    
  public FieldMagnitudeProber(IExperimentModel model,
      final ProbeManager probeManager) {
    super(model, probeManager);
    imageCorrect = new ProbeImage();
    imageCorrect.setClickListener(new ClickResult(true, probeManager));
    imageWrong = new ProbeImage();
    imageWrong.setClickListener(new ClickResult(false, probeManager));
    this.points = new Vector2[] { new Vector2(), new Vector2()};
    this.bFields = new Vector2[] { new Vector2(), new Vector2()};
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
      probeManager.randomizeConfig();
      // Generate two random points P1, P2 in unit circle.
      // If P0.r ~ P1.r AND (P0.x ~ P1.x) OR (P0.y ~ P1.y) try again
      // Scale P0.x, P1.x by magnet width*2 and P0.y, P1.y by magnet height*2
      do {
        generateProbePoints(points);
      } while (!arePointsAcceptable(points, bFields));
      
      createFieldMeterSamples(points, bFields);
      
      if (bFields[0].len() > bFields[1].len()) {
        imageCorrect.x = points[0].x - imageCorrect.width/2;
        imageCorrect.y = points[0].y - imageCorrect.height/2;
        imageWrong.x = points[1].x - imageWrong.width/2;
        imageWrong.y = points[1].y - imageWrong.width/2;
      } else {
        imageCorrect.x = points[1].x - imageCorrect.width/2;
        imageCorrect.y = points[1].y - imageCorrect.height/2;
        imageWrong.x = points[0].x - imageWrong.width/2;
        imageWrong.y = points[0].y - imageWrong.width/2;
      }
    }
    this.visible = activate;
  }

  private boolean haveSimilarMagnitudes(float v1, float v2) {
    if (Math.abs(v1 - v2) < ZERO_TOLERANCE) return true;
    if (Math.abs(v1 - v2) / Math.min(v1, v2) < TOLERANCE) return true;
    return false;
  }
  
  protected boolean arePointsAcceptable(Vector2[] points, Vector2[] bFields) {
    getBField(points[0], bFields[0]);
    getBField(points[1], bFields[1]);
    if (haveSimilarMagnitudes(bFields[0].len(), bFields[1].len())) return false;
    return true;
  }
}