package com.mazalearn.scienceengine.domains.electromagnetism.probe;

import java.util.Arrays;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.probe.Hint;
import com.mazalearn.scienceengine.core.probe.ProbeImage;
import com.mazalearn.scienceengine.core.probe.ProbeManager;

// doubts on direction
// Generate A at "random" point around active elements.
// What is direction of field at A?
// doubts on shielding - not yet addressed
public class FieldDirectionProber extends AbstractFieldProber {
  private final Image image, userField;
  private Vector2[] points, bFields;
  private Hint[] hints = new Hint[] {
      new Hint("The direction of the field is the direction in which a " +
      "free North Pole would move if placed at that point."),
      new Hint("The direction of the field is where the compass needle's North would point.")
  };
  
  public FieldDirectionProber(IScience2DModel science2DModel, final ProbeManager probeManager) {
    super(science2DModel, probeManager);
    
    this.points = new Vector2[] { new Vector2()};
    this.bFields = new Vector2[] { new Vector2()};
    
    userField = new Image(new TextureRegion(new Texture("images/fieldarrow-yellow.png")));
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
        final boolean success = Math.abs(userField.getRotation() - bFields[0].angle()) < TOLERANCE * 100;
        fieldMeterActor.setVisible(true);
        userField.addAction(Actions.sequence(Actions.delay(2f),
            new Action() {
              @Override
              public boolean act(float delta) {
                probeManager.done(success);
                fieldMeterActor.setVisible(false);
                userField.setVisible(false);
                return true;
              }
            }));
      }
    });
    this.addActor(image);
    this.addActor(userField);
  }
  
  @Override
  public String getTitle() {
    return "Click and drag in direction of magnetic field";
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, width, height, probeMode);
    image.setVisible(false);
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      probeManager.setupProbeConfigs(science2DModel.getAllConfigs(), false);
      generateProbePoints(points);
      getBField(points[0], bFields[0]);
      createFieldMeterSamples(points, bFields);
      image.setX(points[0].x - image.getWidth()/2);
      image.setY(points[0].y - image.getHeight()/2);
      bFields[0].nor();
      image.setVisible(true);
      fieldMeterActor.setVisible(false);
    }
    this.setVisible(activate);
  }

  @Override
  public Hint getHint() {
    return null;
  }
}