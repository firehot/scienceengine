package com.mazalearn.scienceengine.core.probe;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

class Dashboard extends Table {
  Label status, scoreLabel;
  int score;
  Dashboard(Skin skin) {
    super(skin, null, "Dashboard");
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
    this.setFillParent(false);
    this.top().center();
    status = new Label("Status", skin) {
      private float increment = 0.01f;
      private float alpha = 1;
      @Override
      public void draw(SpriteBatch batch, float parentAlpha) {
        Color c = batch.getColor();
        batch.setColor(c.r, c.g, c.b, alpha);
        super.draw(batch, alpha);
        batch.setColor(c);
        alpha += increment;
        if (alpha > 1 - increment || alpha <= 0.5f) {
          increment = -increment;
        }
      }      
    };
    scoreLabel = new Label("0", skin);
    this.add("Score").left();
    this.add(scoreLabel).right().fill();
    this.row();
    this.add(status).colspan(2).fill();
    this.row();
  }
  
  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void setStatus(String text) {
    status.setText(text);
  }

  public int getScore() {
    return score;
  }
}