package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;

public class Scoreboard extends Table {
  Label scoreLabel;
  int score;

  Scoreboard(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
    this.setName(ScreenComponent.Scoreboard.name());

    scoreLabel = new Label("0", skin);   
    this.add("Score").top();
    this.row();
    this.add(scoreLabel).width(40).fill().top();
    
    this.setPosition(ScreenComponent.Scoreboard.getX(getWidth()) + getWidth() / 2,
        ScreenComponent.Scoreboard.getY(getHeight()) + getHeight() / 2);
  }

  public int getScore() {
    return score;
  }

  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void resetScore() {
    score = 0;
  }
}