package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.controller.IExperimentController;
import com.mazalearn.scienceengine.services.LevelManager;
import com.mazalearn.scienceengine.services.Profile;
import com.mazalearn.scienceengine.view.IExperimentView;

/**
 * Experiment Home screen - shows all levels for that experiment.
 */
public class ExperimentHomeScreen extends AbstractScreen {

  IExperimentController experimentController;
  private Image[] levelThumbs;
  private LevelManager levelManager;
  private ShapeRenderer shapeRenderer;

  public ExperimentHomeScreen(ScienceEngine scienceEngine, 
      IExperimentController experimentController) {
    super(scienceEngine);
    this.experimentController = experimentController;
    shapeRenderer = new ShapeRenderer();
  }

  @Override
  public void show() {
    super.show();
    
    Table table = super.getTable();
    table.add(experimentController.getName()).colspan(3);
    table.row();
    final IExperimentView experimentView = experimentController.getView();
    Profile profile = scienceEngine.getProfileManager().retrieveProfile();
    int level = Math.max(profile.getCurrentLevelId(), 1);
    levelManager = experimentView.getLevelManager();
    levelManager.setLevel(level);
    levelThumbs = new Image[10];
    for (int i = 1; i <= 10; i++) {
      final int iLevel = i;
      Image levelThumb = new Image(levelManager.getLevelThumbnail(i));
      levelThumb.setClickListener(new ClickListener() {
        @Override
        public void click(Actor actor, float x, float y) {
          levelManager.setLevel(iLevel);
        }
      });
      levelThumbs[i - 1] = levelThumb;
      table.add(levelThumb).pad(5);
      if (i % 3 == 0) {
        table.row();
      }
    }    
    table.row();
    
    TextButton experimentLevelButton = 
        new TextButton("Start", scienceEngine.getSkin());
    table.add(experimentLevelButton).fill().colspan(3);
    experimentLevelButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        Screen experimentLevelScreen = 
            new ExperimentLevelScreen(scienceEngine, levelManager, 
                experimentController);
        scienceEngine.setScreen(experimentLevelScreen);
      }
    });
  }

  public void render(float delta) {
    super.render(delta);
    Image thumbNail = levelThumbs[levelManager.getLevel() - 1];
    if (thumbNail == null) return;
    
    shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
    stage.getSpriteBatch().begin();
    shapeRenderer.begin(ShapeType.Rectangle);
    shapeRenderer.setColor(Color.YELLOW);
    shapeRenderer.rect(thumbNail.x, thumbNail.y, thumbNail.width, thumbNail.height);
    shapeRenderer.end();
    stage.getSpriteBatch().end();
  }
}
