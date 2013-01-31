package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

class Dashboard extends Table {
  TextButton goal, subGoal;
  Label scoreLabel;
  int score;
  private Label timerLabel;
  float timeLeft = 300;

  Dashboard(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }

    this.add(createTutorBoard(skin));
  }

  private Table createTutorBoard(Skin skin) {
    Table tutorTable = new Table(skin);
    tutorTable.setFillParent(false);
    tutorTable.center();
    
    goal = new TextButton("Goal", skin);
    goal.setColor(Color.YELLOW);
    goal.addListener(new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        subGoal.setVisible(!subGoal.isVisible());
      }
    });
    
    subGoal = new TextButton("", skin);
    subGoal.setColor(Color.YELLOW);
    subGoal.getLabel().setWrap(true);
    subGoal.setVisible(false);
    // TODO: BUG in libgdx for wrapped labels ??? hence setting height
    //subGoal.setSize(400, 50); 
    
    scoreLabel = new Label("0", skin);

    timerLabel = new Label("0", skin) {
      @Override
      public void act(float delta) {
        timeLeft -= delta;
        String seconds = String.valueOf(Math.round(timeLeft % 60));
        if (timeLeft < 0) {
          // TODO: goal.setText("Time Up");
        } else {
          this.setText(Math.round(timeLeft / 60) + ":" + "0".substring(0, 2 - seconds.length()) + seconds);
        }
      }
    };
    
    Table t = new Table(skin);
    t.add("Timer");
    t.row();
    t.add(timerLabel).width(40).fill().right();
    tutorTable.add(t).left();
    tutorTable.add(goal).pad(0, 10, 0, 10).width(430).fill();
    goal.getLabel().setWrap(true);
    t = new Table(skin);
    t.add("Score");
    t.row();
    t.add(scoreLabel).width(40).fill().center();
    tutorTable.add(t).right();
    tutorTable.row();
    tutorTable.add("");
    tutorTable.add(subGoal).width(400).height(50);
    return tutorTable;
  }
  
  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void setGoal(String text) {
    goal.setText(text);
  }
  
  public void setSubgoal(String text) {
    if (text == null) {
      subGoal.setVisible(false);
      return;
    }
    subGoal.setText(text);
    subGoal.setVisible(true);
  }

  public int getScore() {
    return score;
  }

  public void resetScore() {
    score = 0;
  }
  
  public void setTimeLimit(int timeLimitSeconds) {
    this.timeLeft = timeLimitSeconds;
  }
}