package com.mazalearn.scienceengine.app.screens;

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
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.controller.IExperimentController;
import com.mazalearn.scienceengine.core.view.IExperimentView;

/**
 * Experiment Home screen - shows all levels for that experiment.
 */
public class ExperimentHomeScreen extends AbstractScreen {

  private static final int NUM_COLUMNS = 3;
  IExperimentController experimentController;
  private Image[] levelThumbs;
  private LevelManager levelManager;
  private ShapeRenderer shapeRenderer;

  public ExperimentHomeScreen(ScienceEngine scienceEngine, 
      IExperimentController experimentController) {
    super(scienceEngine);
    this.experimentController = experimentController;
    shapeRenderer = new ShapeRenderer();
    setBackgroundColor(Color.DARK_GRAY);
  }

  @Override
  public void show() {
    super.show();
    
    Table table = super.getTable();
    
    table.defaults().fill().center();
    table.add(experimentController.getName()).colspan(NUM_COLUMNS);
    table.row();
    final IExperimentView experimentView = experimentController.getView();
    Profile profile = scienceEngine.getProfileManager().retrieveProfile();
    int level = Math.max(profile.getCurrentLevelId(), 1);
    levelManager = experimentView.getLevelManager();
    levelManager.setLevel(level);
    levelThumbs = new Image[10];
    for (int i = 1; i <= 10; i++) {
      final int iLevel = i;
      Image levelThumb = 
          new Image(LevelManager.getThumbnail(experimentController.getName(), i));
      levelThumb.setClickListener(new ClickListener() {
        @Override
        public void click(Actor actor, float x, float y) {
          Screen experimentLevelScreen = 
              new ExperimentLevelScreen(scienceEngine, levelManager, 
                  iLevel, experimentController);
          scienceEngine.setScreen(experimentLevelScreen);
        }
      });
      levelThumbs[i - 1] = levelThumb;
      Table levelTable = table.newTable();
      // TODO: add description of each level
      levelTable.add("");
      levelTable.row();
      levelTable.add(levelThumb);
      table.add(levelTable).pad(5);
      if (i % NUM_COLUMNS == 0) {
        table.row();
      }
    }    
    table.row();
    
    // register the back button
    TextButton backButton = new TextButton("Back to Experiments", scienceEngine.getSkin());
    backButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        scienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
        scienceEngine.setScreen(new ExperimentMenuScreen(scienceEngine));
      }
    });
    table.row();
    table.add(backButton).fill().colspan(NUM_COLUMNS);
  }

  public void render(float delta) {
    super.render(delta);
    Image thumbNail = levelThumbs[levelManager.getLevel() - 1];
    if (thumbNail == null) return;
    
    shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
    stage.getSpriteBatch().begin();
    shapeRenderer.begin(ShapeType.Rectangle);
    shapeRenderer.setColor(Color.YELLOW);
    shapeRenderer.rect(thumbNail.parent.x + thumbNail.x, 
        thumbNail.parent.y + thumbNail.y, thumbNail.width, thumbNail.height);
    shapeRenderer.end();
    stage.getSpriteBatch().end();
  }
}
