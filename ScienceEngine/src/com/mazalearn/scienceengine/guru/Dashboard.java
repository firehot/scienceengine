package com.mazalearn.scienceengine.guru;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.tablelayout.Cell;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.ScreenComponent;

class Dashboard extends Table {
  TextButton topGoal;
  Label scoreLabel;
  int score;
  private Label timerLabel;
  float timeLeft = 300;
  private Table goalTable;
  private Skin skin;
  private Cell<Actor> goalCell;
  private ClickListener clickListener;

  Dashboard(Skin skin) {
    super(skin);
    this.skin = skin;
    if (ScienceEngine.DEV_MODE != DevMode.PRODUCTION) {
      debug();
    }

    this.add(createTutorBoard(skin));
  }

  @SuppressWarnings("unchecked")
  private Table createTutorBoard(Skin skin) {
    Table tutorTable = new Table(skin);
    tutorTable.setFillParent(false);
    tutorTable.center();
    
    goalTable = new Table(skin);
    goalTable.setVisible(false);
    
    clickListener = new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        goalTable.setVisible(!goalTable.isVisible());
        topGoal.setVisible(!topGoal.isVisible());
        goalCell.setWidget(topGoal.isVisible() ? topGoal : goalTable);
        reposition();
      }
    };
    topGoal = createGoalButton("");
    
    scoreLabel = new Label("0", skin);

    timerLabel = new Label("0", skin) {
      @Override
      public void act(float delta) {
        timeLeft -= delta;
        String seconds = String.valueOf(Math.round(timeLeft % 60));
        if (timeLeft < 0) {
          // TODO: topGoal.setText("Time Up");
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
    goalCell = tutorTable.add(topGoal).pad(0, 10, 0, 10).width(ScreenComponent.getScaledX(430)).fill();
    t = new Table(skin);
    t.add("Score").top();
    t.row();
    t.add(scoreLabel).width(40).fill().top();
    tutorTable.add(t).right().top();
    tutorTable.row();
    tutorTable.add("");
    return tutorTable;
  }

  private TextButton createGoalButton(String goal) {
    TextButton goalButton = new TextButton(goal, skin);
    goalButton.setColor(Color.YELLOW);
    goalButton.addListener(clickListener);
    goalButton.getLabel().setWrap(true);
    return goalButton;
  }
  
  private void reposition() {
    invalidate();
    setX(ScreenComponent.Dashboard.getX(getPrefWidth()) + getPrefWidth() / 2);
    setY(ScreenComponent.Dashboard.getY(getPrefHeight()) + getPrefHeight() / 2);    
  }
  
  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void setGoals(List<String> goals) {
    goalTable.clear();
    if (goals == null) {
      topGoal.setVisible(false);
      topGoal.setText("");
      return;
    }
    for (String goal: goals) {
      goalTable.add(createGoalButton(goal)).width(ScreenComponent.getScaledX(430)).fill();
      goalTable.row();
    }
    topGoal.setText(goals.get(goals.size() - 1));
    topGoal.addAction(Actions.sequence(
        Actions.alpha(0),
        Actions.alpha(1, 2)));
    reposition();
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
    setGoals(null);
  }
}