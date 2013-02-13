package com.mazalearn.scienceengine.guru;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
  private SubgoalNavigator subgoalNavigator;
  private ClickListener clickListener;
  private ITutor activeTutor;

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
    
    clickListener = new ClickListener() {
      public void clicked (InputEvent event, float x, float y) {
        if (subgoalNavigator.isVisible()) {
          subgoalNavigator.setVisible(false);
          return;
        }
        // Bring subgoal navigator to top, and dashboard above it.
        subgoalNavigator.setVisible(true);
        subgoalNavigator.setActiveTutor(activeTutor);
        getStage().addActor(subgoalNavigator);
        getStage().addActor(Dashboard.this);
      }
    };
    goal = new TextButton("", skin);
    goal.setColor(Color.YELLOW);
    goal.addListener(clickListener);
    goal.getLabel().setWrap(true);
    
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
    tutorTable.add(goal).pad(0, 10, 0, 10).width(ScreenComponent.getScaledX(430)).fill();
    t = new Table(skin);
    t.add("Score").top();
    t.row();
    t.add(scoreLabel).width(40).fill().top();
    tutorTable.add(t).right().top();
    tutorTable.row();
    tutorTable.add("");
    return tutorTable;
  }

  private void reposition() {
    invalidate();
    setX(ScreenComponent.Dashboard.getX(getPrefWidth()) + getPrefWidth() / 2);
    setY(ScreenComponent.Dashboard.getY(getPrefHeight()) + getPrefHeight() / 2);    
  }
  
  public void setActiveTutor(ITutor activeTutor) {
    this.activeTutor = activeTutor;
    this.goal.setText(activeTutor.getGoal());
    this.goal.addAction(Actions.sequence(
        Actions.alpha(0),
        Actions.alpha(1, 2)));
    reposition();
  }

  public void clearActiveTutor() {
    activeTutor = null;
    goal.setVisible(false);
    goal.setText("");
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
  
  public void setTimeLimit(int timeLimitSeconds) {
    this.timeLeft = timeLimitSeconds;
  }

  public void setSubgoalNavigator(SubgoalNavigator subgoalNavigator) {
    this.subgoalNavigator = subgoalNavigator;
  }  

}