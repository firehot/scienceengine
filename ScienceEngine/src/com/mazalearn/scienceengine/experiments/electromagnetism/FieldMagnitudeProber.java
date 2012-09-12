package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.view.IDoneCallback;

// doubts on magnitude
// Generate A, B at two "random" points around magnet
// Is the field stronger at A or B?
class FieldMagnitudeProber extends AbstractProber {
  final Image imageCorrect, imageWrong;
  private final Actor barMagnet;
  
  public FieldMagnitudeProber(Skin skin, Actor barMagnet, final IDoneCallback doneCallback) {
    super();
    this.barMagnet = barMagnet;
    TextureRegion questionMark = new TextureRegion(new Texture("images/questionmark.png"));
    imageCorrect = new Image(questionMark);
    imageCorrect.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        doneCallback.done(true);
      }
    });
    imageWrong = new Image(questionMark);
    imageWrong.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        doneCallback.done(false);
      }
    });
    this.addActor(imageCorrect);
    this.addActor(imageWrong);
  }
  
  @Override
  public String getTitle() {
    return "Click where the magnetic field is stronger";
  }
  
  @Override
  public void activate(boolean activate) {
    if (activate) {
      imageCorrect.x = barMagnet.x;
      imageCorrect.y = barMagnet.y;
      imageWrong.x = barMagnet.x + barMagnet.width * MathUtils.random();
      imageWrong.y = barMagnet.y + barMagnet.height * MathUtils.random();
    }
    this.visible = activate;
  }
}