package com.mazalearn.scienceengine.domains.electromagnetism.tutor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.tutor.ITutor;
import com.mazalearn.scienceengine.tutor.ProbeImage;

// doubts on magnitude
// Generate A, B at two "random" points around magnet
// Is the field stronger at A or B?
public class FieldMagnitudeProber extends AbstractFieldProber {
  
  private final class ClickResult extends ClickListener {
    private final boolean success;
 
    private ClickResult(boolean success) {
      this.success = success;
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
                  if (success) {
                    imageCorrect.setVisible(false);
                    imageWrong.setVisible(false);
                  }
                  systemReadyToFinish(success);
                  return true;
                }
              })
          );
    }
  };

  private final Image imageCorrect, imageWrong;
  // Temporary vectors
  private Vector2[] points;
  private Vector3[] bFields;

  public FieldMagnitudeProber(IScience2DController science2DController,
      ITutorType tutorType, ITutor parent, String goal, String id, Array<?> components, Array<?> configs, 
      int deltaSuccessScore, int deltaFailureScore, String[] hints) {
    super(science2DController, tutorType, parent, goal, id, components, configs, deltaSuccessScore, deltaFailureScore, hints);
    
    imageCorrect = new ProbeImage();
    imageCorrect.addListener(new ClickResult(true));
    imageWrong = new ProbeImage();
    imageWrong.addListener(new ClickResult(false));
    this.points = new Vector2[] { new Vector2(), new Vector2()};
    this.bFields = new Vector3[] { new Vector3(), new Vector3()};
    this.addActor(imageCorrect);
    this.addActor(imageWrong);
  }
  
  @Override
  public void prepareToTeach(ITutor childTutor) {
    super.prepareToTeach(childTutor);
    imageCorrect.setVisible(false);
    imageWrong.setVisible(false);
  }
  
  @Override
  public void teach() {
    super.teach();
    science2DController.getGuru().setupProbeConfigs(
        science2DController.getModel().getAllConfigs().values(), false);
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

  private boolean haveSimilarMagnitudes(float v1, float v2) {
    if (Math.abs(v1 - v2) < ZERO_TOLERANCE) return true;
    if (Math.abs(v1 - v2) / Math.min(v1, v2) < TOLERANCE) return true;
    return false;
  }
  
  protected boolean arePointsAcceptable(Vector2[] points, Vector3[] bFields) {
    getBField(points[0], bFields[0]);
    getBField(points[1], bFields[1]);
    if (haveSimilarMagnitudes(bFields[0].len(), bFields[1].len())) return false;
    return true;
  }
}