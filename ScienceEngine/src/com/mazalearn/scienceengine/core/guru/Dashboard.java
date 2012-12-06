package com.mazalearn.scienceengine.core.guru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

class Dashboard extends Table {
  Label status, scoreLabel;
  int score;
  private Label timerLabel;
  float timeLimit = 300;

  Dashboard(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
    this.setFillParent(false);
    this.top().center();
    LabelStyle style = skin.get(LabelStyle.class);
    style.fontColor = Color.YELLOW;
    status = new Label("Challenge", style) {
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
    style.fontColor = Color.WHITE;
    scoreLabel = new Label("0", skin);

    timerLabel = new Label("0", skin) {
      @Override
      public void act(float delta) {
        timeLimit -= delta;
        this.setText(String.valueOf(Math.round(timeLimit / 60) + ":" + String.valueOf(Math.round(timeLimit % 60))));
        if (timeLimit < 0) {
          status.setText("Time Up");
        }
      }
    };
    
    this.add(new Label("", skin)).pad(30, 0, 0, 0);
    this.row();
    this.add("Score").left();
    this.add(scoreLabel).right().fill();
    this.add("Time Left").right().pad(10);
    this.add(timerLabel).right().fill();
    this.row();
    
    this.add("Challenge").pad(10, 0, 0, 10).left();
    this.add(status).pad(10, 0, 0, 0).colspan(3).fill();
    this.row();
  }
  
  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void setStatus(String text) {
    // For a table, x and y are at center, top of table - not at bottom left
    this.setY(getParent().getHeight() - getPrefHeight() / 2);
    this.setX(getParent().getWidth()/2);
    status.setText(text);
  }

  public int getScore() {
    return score;
  }

  public void resetScore() {
    score = 0;
  }
  
  public void setTimeLimit(int timeLimitSeconds) {
    this.timeLimit = timeLimitSeconds;
  }
}