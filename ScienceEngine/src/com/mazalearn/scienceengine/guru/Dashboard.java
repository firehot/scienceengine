package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

class Dashboard extends Table {
  TextButton goal;
  Label scoreLabel;
  int score;
  private Label timerLabel;
  float timeLimit = 300;

  Dashboard(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
    this.setFillParent(false);
    this.center();
    goal = new TextButton("Goal", skin) {
      private float increment = 0f; // TODO: 0.01f;
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
    goal.setColor(Color.YELLOW);
    scoreLabel = new Label("0", skin);

    timerLabel = new Label("0", skin) {
      @Override
      public void act(float delta) {
        timeLimit -= delta;
        this.setText(String.format("%2d:%02d", Math.round(timeLimit / 60), Math.round(timeLimit % 60)));
        if (timeLimit < 0) {
          // TODO: goal.setText("Time Up");
        }
      }
    };
    
    Table t = new Table(skin);
    t.add("Timer");
    t.row();
    t.add(timerLabel).width(40).fill().right();
    this.add(t).left();
    this.add(goal).pad(10).width(430).fill();
    goal.getLabel().setWrap(true);
    t = new Table(skin);
    t.add("Score");
    t.row();
    t.add(scoreLabel).width(40).fill().center();
    this.add(t).right();
  }
  
  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void setStatus(String text) {
    // For a table, x and y are at center, top of table - not at bottom left
    this.setY(getParent().getHeight() - getPrefHeight() / 2);
    this.setX(getParent().getWidth()/2);
    goal.setText(text);
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