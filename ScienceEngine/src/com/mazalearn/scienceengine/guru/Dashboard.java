package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;

class Dashboard extends Table {
  TextButton goal;
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
        //subGoal.setVisible(!subGoal.isVisible());
      }
    });
    
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
    t.add(""); //t.add("Timer");
    t.row();
    t.add("").width(40).fill().right();// t.add(timerLabel).width(40).fill().right();
    tutorTable.add(t).left();
    tutorTable.add(goal).pad(0, 10, 0, 10).width(ScreenComponent.getScaledX(430)).fill();
    goal.getLabel().setWrap(true);
    t = new Table(skin);
    t.add("Score").top();
    t.row();
    t.add(scoreLabel).width(40).fill().top();
    tutorTable.add(t).right().top();
    tutorTable.row();
    tutorTable.add("");
    return tutorTable;
  }
  
  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void setGoal(String text) {
    if (text == null) {
      goal.setVisible(false);
      goal.setText("");
      return;
    }
    if (goal.getText().toString().equals(text)) return;
    goal.setText(text);
    goal.setVisible(true);
    goal.addAction(Actions.sequence(
        Actions.alpha(0, 1),
        Actions.alpha(1, 2)));
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

  public void clearGoals() {
    setGoal(null);
  }
}