package com.mazalearn.scienceengine.core.probe;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.experiments.electromagnetism.probe.AbstractFieldProber;

/**
 * Cycles through the probers - probing the user with each one.
 * 
 * @author sridhar
 * 
 */
public class ProbeManager extends Group implements IDoneCallback {
  private final class ScoreImage extends Image {
    private int score;
    private final boolean success;
    private BitmapFont font;
    private float increment = 0.015f;

    private ScoreImage(Texture texture, Skin skin, boolean success) {
      super(texture);
      this.visible = false;
      this.success = success;
      font = skin.getFont("default-font");
    }

    public void initialize(float x, float y, int score) {
      this.x = x;
      this.y = y;
      this.score = score;
      this.visible = true;
    }

    public void act(float delta) {
      this.y += success ? 2 : -2;
      this.rotation += increment;
      if (this.rotation >= 5) {
        increment = -1;
      } else if (this.rotation <= -5) {
        increment = 1;
      }
    }

    public void draw(SpriteBatch batch, float parentAlpha) {
      super.draw(batch, parentAlpha);
      Color c = font.getColor();
      font.setColor(Color.BLACK);
      font.draw(batch, String.valueOf(score), x + width/2 - 10, y + height/2);
      font.setColor(c);
    }
  }

  int current = 0;
  protected Dashboard dashboard;
  private List<AbstractFieldProber> probers = new ArrayList<AbstractFieldProber>();
  private final IDoneCallback doneCallback;
  private final SoundManager soundManager;
  private final ScoreImage successImage, failureImage;

  public ProbeManager(final Skin skin, float width, float height,
      IDoneCallback doneCallback) {
    super();
    this.dashboard = new Dashboard(skin);
    this.addActor(dashboard);
    this.doneCallback = doneCallback;
    this.soundManager = ScienceEngine.getSoundManager();
    this.x = 0;
    this.y = 0;
    this.width = width;
    this.height = height;
    // For a table, x and y are at center, top of table - not at bottom left
    this.dashboard.y = height;
    this.dashboard.x = width/2;
     
    this.successImage = new ScoreImage(new Texture("images/greenballoon.png"), skin, true);
    this.failureImage = new ScoreImage(new Texture("images/redballoon.png"), skin, false);
    this.addActor(successImage);
    this.addActor(failureImage);
  }

  public void addProbe(AbstractFieldProber prober) {
    probers.add(prober);
    this.addActor(prober);
    prober.activate(false);
  }

  public void startChallenge() {
    // Set up space for probers
    for (Actor prober: probers) {
      prober.x = x;
      prober.y = y;
      prober.width = width;
      prober.height = height;
    }
    probers.get(current).activate(true);
    dashboard.setStatus(probers.get(current).getTitle());
  }

  /**
   * IDoneCallback interface implementation
   */
  public void done(boolean success) {
    soundManager.play(
        success ? ScienceEngineSound.SUCCESS : ScienceEngineSound.FAILURE);
    dashboard.addScore(success ? 10 : -5);
    if (success) {
      successImage.initialize(width/2, height/2, 10);
    } else {
      failureImage.initialize(width/2, height/2, -5);
    }
    probers.get(current).activate(false);
    if (dashboard.getScore() > 100) {
      doneCallback.done(true);
      return;
    }
    current = (current + 1) % probers.size();
    probers.get(current).activate(true);
    dashboard.setStatus(probers.get(current).getTitle());
  }

  public void setTitle(String text) {
    dashboard.setStatus(text);
  }

  public Actor getDashboard() {
    return dashboard;
  }
}