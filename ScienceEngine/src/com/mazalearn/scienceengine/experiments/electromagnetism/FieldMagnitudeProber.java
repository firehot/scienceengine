package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.box2d.ScienceActor;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.BarMagnet;
import com.mazalearn.scienceengine.view.IDoneCallback;

// doubts on magnitude
// Generate A, B at two "random" points around magnet
// Is the field stronger at A or B?
class FieldMagnitudeProber extends AbstractProber {
  private static final float TOLERANCE = 0.3f;
  final Image imageCorrect, imageWrong;
  private final ScienceActor barMagnetView;
  // Temporary vectors
  Vector2 pos1 = new Vector2(), pos2 = new Vector2(), pos = new Vector2();
  Vector2 bField = new Vector2();
  
  public FieldMagnitudeProber(Skin skin, ScienceActor barMagnetView, 
      final IDoneCallback doneCallback) {
    super();
    this.barMagnetView = barMagnetView;
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
      // Generate two random points P1, P2 in unit circle.
      // If P1.r ~ P2.r AND (P1.x ~ P2.x) OR (P1.y ~ P2.y) try again
      // Scale P1.x, P2.x by magnet width*2 and P1.y, P2.y by magnet height*2
      boolean areTooClose = true;
      while (areTooClose) {
        generateProbePoint(pos1);
        generateProbePoint(pos2);
        areTooClose = approxEquals(pos1.len(), pos2.len()) && 
            (approxEquals(pos1.x, pos2.x) || approxEquals(pos1.y, pos2.y));
      }
      float magnitude1 = getBFieldMagnitude(pos1);
      float magnitude2 = getBFieldMagnitude(pos2);
      System.out.println("correct " + magnitude1 + " wrong " + magnitude2);
      if (magnitude1 > magnitude2) {
        imageCorrect.x = pos1.x - imageCorrect.width/2;
        imageCorrect.y = pos1.y - imageCorrect.height/2;
        imageWrong.x = pos2.x - imageWrong.width/2;
        imageWrong.y = pos2.y - imageWrong.width/2;
      } else {
        imageCorrect.x = pos2.x - imageCorrect.width/2;
        imageCorrect.y = pos2.y - imageCorrect.height/2;
        imageWrong.x = pos1.x - imageWrong.width/2;
        imageWrong.y = pos1.y - imageWrong.width/2;
      }
    }
    this.visible = activate;
  }

  private void generateProbePoint(Vector2 pos) {
    pos.set(MathUtils.random(2f) - 1, MathUtils.random(2f) - 1);
    pos.x *= barMagnetView.width;
    pos.y *= barMagnetView.height;
    pos.add(barMagnetView.x + barMagnetView.width/2, 
        barMagnetView.y + barMagnetView.height/2);
  }

  private boolean approxEquals(float len1, float len2) {
    return Math.abs(len1 - len2) < TOLERANCE;
  }

  private float getBFieldMagnitude(Vector2 viewPos) {
    barMagnetView.getBox2DPositionFromViewPosition(pos, viewPos, 0);
    BarMagnet b = (BarMagnet) barMagnetView.getBody();
    b.getBField(pos, bField /* output */);
    return bField.len();
  }
}