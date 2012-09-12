package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.view.ProbeManager;

// doubts on direction
// Generate A at "random" point around magnet
// What is direction of field at A?
// doubts on shielding - not yet addressed
class FieldDirectionProber extends AbstractProber {
  private final Image image;
  private final Actor barMagnet;
  
  public FieldDirectionProber(Skin skin, Actor barMagnet, final ProbeManager manager) {
    super();
    this.barMagnet = barMagnet;
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
        manager.probeDone(true);
      }
    };
    this.addActor(image);
  }
  
  @Override
  public String getTitle() {
    return "Click and drag in direction of magnetic field";
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      image.x = barMagnet.x + barMagnet.width * MathUtils.random();
      image.y = barMagnet.y + barMagnet.height * MathUtils.random();
    }
    this.visible = activate;
  }
}