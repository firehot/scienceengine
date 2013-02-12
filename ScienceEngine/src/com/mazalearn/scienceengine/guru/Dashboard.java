package com.mazalearn.scienceengine.guru;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.ScreenComponent;

class Dashboard extends Table {
  TextButton topGoal;
  Label scoreLabel;
  int score;
  private Label timerLabel;
  float timeLeft = 300;
  private Group stageGroup;
  private Skin skin;
  private ClickListener clickListener;

  Dashboard(Skin skin) {
    super(skin);
    this.skin = skin;
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
        stageGroup.setVisible(!stageGroup.isVisible());
      }
    };
    topGoal = new TextButton("", skin);
    topGoal.setColor(Color.YELLOW);
    topGoal.addListener(clickListener);
    topGoal.getLabel().setWrap(true);
    
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
    tutorTable.add(topGoal).pad(0, 10, 0, 10).width(ScreenComponent.getScaledX(430)).fill();
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
  
  public void addScore(int deltaScore) {
    score += deltaScore;
    scoreLabel.setText(String.valueOf(score));
  }
  
  public void setGoal(String goal) {
    if (goal == null) {
      topGoal.setVisible(false);
      topGoal.setText("");
      return;
    }
    topGoal.setText(goal);
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
    setGoal(null);
  }

  private TextButton createStageButton(String name, float width, float height, float x, float y) {
    TextButton stageButton = new TextButton(name, skin);
    stageButton.addListener(clickListener);
    stageButton.getLabel().setWrap(true);
    stageButton.setSize(width, height);
    stageButton.setPosition(x, y);
    return stageButton;
  }
  
  public void setStages(List<String> stages) {
    stageGroup = new Group() {
      Color c = new Color(Color.WHITE.r, Color.WHITE.g, Color.WHITE.b, 0.5f);
      TextureRegion gray = ScreenUtils.createTexture(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT, c);
      public void draw(SpriteBatch batch, float parentAlpha) {
        batch.draw(gray, getX(), getY());
        super.draw(batch, parentAlpha);
      }
    };
    stageGroup.setVisible(false);
    stageGroup.setPosition(0, 0);
    stageGroup.setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
    this.getStage().addActor(stageGroup);
    
    float height = ScreenComponent.VIEWPORT_HEIGHT / (stages.size() + 3);
    float width = 2.5f * ScreenComponent.VIEWPORT_WIDTH / (stages.size() + 3);
    for (int i = 0; i < stages.size(); i++) {
      stageGroup.addActor(createStageButton(stages.get(i), width, height, width / 2.5f * (i + 1), height * (i + 1)));
    }
  }
}