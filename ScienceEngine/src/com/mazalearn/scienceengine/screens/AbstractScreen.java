package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * The base class for all scienceEngine screens.
 */
public abstract class AbstractScreen implements Screen {
  // the fixed viewport dimensions (ratio: 1.6)
  public static final int VIEWPORT_WIDTH = 800,
      VIEWPORT_HEIGHT = 480;
  public static final int MENU_VIEWPORT_WIDTH = 800,
      MENU_VIEWPORT_HEIGHT = 480;

  protected final ScienceEngine scienceEngine;
  protected Stage stage;

  private BitmapFont font;
  private SpriteBatch batch;
  private Skin skin;
  private TextureAtlas atlas;
  private Table table;

  public AbstractScreen(ScienceEngine game, Stage stage) {
    this.scienceEngine = game;
  }
  
  public AbstractScreen(ScienceEngine game) {
    this.scienceEngine = game;
    int width = (isExperimentScreen() ? VIEWPORT_WIDTH : MENU_VIEWPORT_WIDTH);
    int height = (isExperimentScreen() ? VIEWPORT_HEIGHT : MENU_VIEWPORT_HEIGHT);
    this.stage = new Stage(width, height, false);
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    Gdx.input.setInputProcessor(stage);    
  }

  protected String getName() {
    return getClass().getName();
  }

  protected boolean isExperimentScreen() {
    return false;
  }

  // Lazily loaded collaborators

  public BitmapFont getFont() {
    if (font == null) {
      font = new BitmapFont();
    }
    return font;
  }

  public SpriteBatch getBatch() {
    if (batch == null) {
      batch = new SpriteBatch();
    }
    return batch;
  }

  public TextureAtlas getAtlas() {
    if (atlas == null) {
      atlas = new TextureAtlas(Gdx.files.internal("image-atlases/pages.atlas"));
    }
    return atlas;
  }

  protected Table getTable() {
    if (table == null) {
      table = new Table(scienceEngine.getSkin());
      table.setFillParent(true);
      if (ScienceEngine.DEV_MODE != ScienceEngine.DevMode.PRODUCTION) {
        table.debug();
      }
      stage.addActor(table);
    }
    return table;
  }

  // Screen implementation

  @Override
  public void show() {
    Gdx.app.log(ScienceEngine.LOG, "Showing screen: " + getName());

    // set the stage as the input processor
    Gdx.input.setInputProcessor(stage);
  }

  @Override
  public void resize(int width, int height) {
    Gdx.app.log(ScienceEngine.LOG, "Resizing screen: " + getName() + 
        " to: " + width + " x " + height);
  }

  @Override
  public void render(float delta) {
    stage.act(delta);
    clearScreen(Color.BLACK);
    stage.draw();
    Table.drawDebug(stage);
  }

  public void clearScreen(Color color) {
    Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void hide() {
    Gdx.app.log(ScienceEngine.LOG, "Hiding screen: " + getName());

    // dispose the screen when leaving the screen;
    // note that the dipose() method is not called automatically by the
    // framework, so we must figure out when it's appropriate to call it
    dispose();
  }

  @Override
  public void pause() {
    Gdx.app.log(ScienceEngine.LOG, "Pausing screen: " + getName());
  }

  @Override
  public void resume() {
    Gdx.app.log(ScienceEngine.LOG, "Resuming screen: " + getName());
  }

  @Override
  public void dispose() {
    Gdx.app.log(ScienceEngine.LOG, "Disposing screen: " + getName());

    // the following call disposes the screen's stage, but on my computer it
    // crashes the scienceEngine so I commented it out; more info can be found at:
    // http://www.badlogicgames.com/forum/viewtopic.php?f=11&t=3624
    // stage.dispose();

    // as the collaborators are lazily loaded, they may be null
    if (font != null)
      font.dispose();
    if (batch != null)
      batch.dispose();
    if (skin != null)
      skin.dispose();
    if (atlas != null)
      atlas.dispose();
  }

  public Skin getSkin() {
    return scienceEngine.getSkin();
  }
}
