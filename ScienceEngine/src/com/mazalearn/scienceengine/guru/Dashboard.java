package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;

class Dashboard extends Table {
  Label scoreLabel;
  int score;

  Dashboard(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }

    this.add(createTutorBoard(skin));
    this.setPosition(ScreenComponent.Dashboard.getX(getWidth()),
        ScreenComponent.Dashboard.getY(getHeight()));
  }

  private Table createTutorBoard(Skin skin) {
    Table tutorTable = new Table(skin);
    tutorTable.setFillParent(false);
    tutorTable.center();
    
    scoreLabel = new Label("0", skin);
    
    Table t = new Table(skin);
    t.add(""); //t.add("Timer");
    t.row();
    t.add("").width(40).fill().right();// t.add(timerLabel).width(40).fill().right();
    tutorTable.add(t).left();
    tutorTable.add("").pad(0, 10, 0, 10).width(ScreenComponent.getScaledX(430)).fill();
    t = new Table(skin);
    t.add("Score").top();
    t.row();
    t.add(scoreLabel).width(40).fill().top();
    tutorTable.add(t).right().top();
    tutorTable.row();
    tutorTable.add("");
    return tutorTable;
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