package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.view.IDoneCallback;

// doubts on direction
// Generate A at "random" point around magnet
// What is direction of field at A?
// doubts on shielding - not yet addressed
class FieldDirectionProber extends AbstractProber {
  protected static final float TOLERANCE = 0.3f;
  private final Image image;
  private final Box2DActor barMagnetView;
  private Vector2 pos = new Vector2(), pos1 = new Vector2(), bField = new Vector2();
  
  public FieldDirectionProber(Skin skin, Box2DActor barMagnetView, 
      final IDoneCallback doneCallback) {
    super();
    this.barMagnetView = barMagnetView;
    TextureRegion questionMark = 
        new TextureRegion(new Texture("images/questionmark.png"));
    image = new Image(questionMark) {
      Vector2 lastTouch = new Vector2();
      @Override
      public boolean touchDown(float x, float y, int pointer) {
        lastTouch.set(x, y);
        return true;
      }
      
      @Override
      public void touchUp(float x, float y, int pointer) {
        lastTouch.sub(x, y);
        float val = lastTouch.nor().dot(bField); // Should be -1
        boolean success = Math.abs(val + 1) < TOLERANCE;
        doneCallback.done(true);
      }
    };
    this.addActor(image);
  }
  
  @Override
  public String getTitle() {
    return "Click and drag in direction of magnetic field";
  }
  
  private void generateProbePoint(Vector2 pos) {
    pos.set(MathUtils.random(2f) - 1, MathUtils.random(2f) - 1);
    pos.x *= barMagnetView.width;
    pos.y *= barMagnetView.height;
    pos.add(barMagnetView.x + barMagnetView.width/2, 
        barMagnetView.y + barMagnetView.height/2);
  }

  private void getBFieldDirection(Vector2 pos) {
    pos1.set(pos); 
    barMagnetView.getWorldPointFromView(pos1, 0);
    BarMagnet b = (BarMagnet) barMagnetView.getBody();
    b.getBField(pos1, bField /* output */);
    bField.nor();
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      generateProbePoint(pos);
      getBFieldDirection(pos);
      image.x = pos.x;
      image.y = pos.y;
    }
    this.visible = activate;
  }
}