package com.mazalearn.scienceengine.domains.electromagnetism.probe;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.guru.IDoneCallback;
import com.mazalearn.scienceengine.guru.ProbeImage;

// doubts on magnitude
// Generate A, B at two "random" points around magnet
// Is the field stronger at A or B?
public class FieldMagnitudeProber extends AbstractFieldProber {
  
  private final class ClickResult extends ClickListener {
    private final IDoneCallback doneCallback;
    private final boolean success;
 
    private ClickResult(boolean success, IDoneCallback doneCallback) {
      this.success = success;
      this.doneCallback = doneCallback;
    }

    @Override
    public void clicked(InputEvent event, float x, float y) {
      fieldMeterActor.setVisible(true);
      fieldMeterActor.addAction(
          Actions.sequence(
              Actions.delay(2f),
              new Action() {
                @Override
                public boolean act(float delta) {
                  fieldMeterActor.setVisible(false);
                  doneCallback.done(success);
                  netSuccesses += success ? 1 : -1;
                  return true;
                }
              })
          );
    }
  };

  private final Image imageCorrect, imageWrong;
  // Temporary vectors
  private Vector2[] points;
  private Vector2[] bFields;

  public FieldMagnitudeProber(IScience2DController science2DController,
      String goal, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore) {
    super(science2DController, goal, components, configs, deltaSuccessScore, deltaFailureScore);
    this.hints = new String[] {
        "The field is stronger closer to the object generating the field",
        "The field is stronger if the current or magnet strength is larger"
    };
    
    imageCorrect = new ProbeImage();
    imageCorrect.addListener(new ClickResult(true, science2DController.getGuru()));
    imageWrong = new ProbeImage();
    imageWrong.addListener(new ClickResult(false, science2DController.getGuru()));
    this.points = new Vector2[] { new Vector2(), new Vector2()};
    this.bFields = new Vector2[] { new Vector2(), new Vector2()};
    this.addActor(imageCorrect);
    this.addActor(imageWrong);
  }
  
  @Override
  public void reinitialize(boolean probeMode) {
    super.reinitialize(probeMode);
    imageCorrect.setVisible(false);
    imageWrong.setVisible(false);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      science2DController.getGuru().setupProbeConfigs(
          science2DController.getModel().getAllConfigs(), false);
      // Generate two random points P1, P2 in unit circle.
      // If P0.r ~ P1.r AND (P0.x ~ P1.x) OR (P0.y ~ P1.y) try again
      // Scale P0.x, P1.x by magnet width*2 and P0.y, P1.y by magnet height*2
      do {
        generateProbePoints(points);
      } while (!arePointsAcceptable(points, bFields));
      
      createFieldMeterSamples(points, bFields);
      
      if (bFields[0].len() > bFields[1].len()) {
        imageCorrect.setX(points[0].x - imageCorrect.getWidth()/2);
        imageCorrect.setY(points[0].y - imageCorrect.getHeight()/2);
        imageWrong.setX(points[1].x - imageWrong.getWidth()/2);
        imageWrong.setY(points[1].y - imageWrong.getWidth()/2);
      } else {
        imageCorrect.setX(points[1].x - imageCorrect.getWidth()/2);
        imageCorrect.setY(points[1].y - imageCorrect.getHeight()/2);
        imageWrong.setX(points[0].x - imageWrong.getWidth()/2);
        imageWrong.setY(points[0].y - imageWrong.getWidth()/2);
      }
      imageWrong.setVisible(true);
      imageCorrect.setVisible(true);
      fieldMeterActor.setVisible(false);
    }
    this.setVisible(activate);
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