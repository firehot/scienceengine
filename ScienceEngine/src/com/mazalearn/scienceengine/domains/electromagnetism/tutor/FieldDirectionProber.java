package com.mazalearn.scienceengine.domains.electromagnetism.tutor;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.tutor.ITutor;
import com.mazalearn.scienceengine.tutor.ProbeImage;

// doubts on direction
// Generate A at "random" point around active elements.
// What is direction of field at A?
// doubts on shielding - not yet addressed
public class FieldDirectionProber extends AbstractFieldProber {
  private final Image image, userField;
  private Vector2[] points;
  private Vector2 bField2 = new Vector2();
  private Vector3[] bFields;

  public FieldDirectionProber(final IScience2DController science2DController,
      ITutorType tutorType, Topic topic, final ITutor parent, String goal, String id, Array<?> components, Array<?> configs, 
      String[] hints, String[] explanation, String[] refs) {
    super(science2DController, tutorType, topic, parent, goal, id, components, configs, 
        hints, explanation, refs);
    
    this.points = new Vector2[] { new Vector2()};
    this.bFields = new Vector3[] { new Vector3()};
    
    userField = new Image(ScienceEngine.getTextureRegion("fieldarrow-yellow"));
    userField.setVisible(false);
    userField.setWidth(userField.getWidth() * 2);
    userField.setHeight(userField.getHeight() * 2);
    userField.setOriginX(0);
    userField.setOriginY(userField.getHeight()/2);
    
    image = new ProbeImage();
    image.addListener(new ClickListener() {
      Vector2 lastTouch = new Vector2(), currentTouch = new Vector2();
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        lastTouch.set(x, y);
        userField.setVisible(true);
        userField.setX(image.getX() + image.getWidth()/2);
        userField.setY(image.getY() + image.getHeight()/3);
        return true;
      }
      
      @Override
      public void touchDragged(InputEvent event, float x, float y, int pointer) {
        currentTouch.set(x, y);
        currentTouch.sub(lastTouch);
        userField.setRotation(currentTouch.angle());
      }
      
      @Override
      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        bField2.set(bFields[0].x, bFields[0].y); // Ignoring z
        final boolean success = Math.abs(userField.getRotation() - bField2.angle()) < TOLERANCE * 100;
        fieldMeterActor.setVisible(true);
        userField.addAction(Actions.sequence(Actions.delay(2f),
            new Action() {
              @Override
              public boolean act(float delta) {
                fieldMeterActor.setVisible(false);
                userField.setVisible(false);
                if (success) {
                  image.setVisible(false);
                }
                systemReadyToFinish(success);
                return true;
              }
            }));
      }
    });
    this.addActor(image);
    this.addActor(userField);
  }
  
  @Override
  public void prepareToTeach(ITutor childTutor) {
    super.prepareToTeach(childTutor);
    image.setVisible(false);
  }
  
  @Override
  public void teach() {
    super.teach();
    science2DController.getGuru().setupProbeConfigs(
        science2DController.getModel().getAllConfigs().values(), false);
    generateProbePoints(points);
    getBField(points[0], bFields[0]);
    createFieldMeterSamples(points, bFields);
    image.setX(points[0].x - image.getWidth()/2);
    image.setY(points[0].y - image.getHeight()/2);
    bFields[0].nor();
    image.setVisible(true);
    fieldMeterActor.setVisible(false);
  }

}