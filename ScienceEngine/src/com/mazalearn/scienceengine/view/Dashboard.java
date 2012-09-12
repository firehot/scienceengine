package com.mazalearn.scienceengine.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

class Dashboard extends Table {
  Label title, scoreLabel;
  int score;
  Dashboard(Skin skin) {
    super(skin, null, "Dashboard");
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }
    this.setFillParent(true);
    this.top().center();
    title = new Label("Probe title", skin);
    scoreLabel = new Label("0", skin);
    this.add("Score").left();
    this.add(scoreLabel).right().fill();
    this.row();
    this.add(title).colspan(2).fill();
    this.row();
  }
  
  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void setTitle(String text) {
    title.setText(text);
  }
}