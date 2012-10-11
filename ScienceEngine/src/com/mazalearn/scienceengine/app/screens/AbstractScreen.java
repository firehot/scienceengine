package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;

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
  private Table table;
  private Color backgroundColor = Color.BLACK;

  public AbstractScreen(ScienceEngine scienceEngine, Stage stage) {
    this.scienceEngine = scienceEngine;
  }
  
  public AbstractScreen(ScienceEngine game) {
    this.scienceEngine = game;
    int width = (isExperimentScreen() ? VIEWPORT_WIDTH : MENU_VIEWPORT_WIDTH);
    int height = (isExperimentScreen() ? VIEWPORT_HEIGHT : MENU_VIEWPORT_HEIGHT);
    this.stage = new Stage(width, height, false);
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Keys.ALT_LEFT) {
          if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
            ScienceEngine.DEV_MODE = DevMode.DESIGN;
          } else if (ScienceEngine.DEV_MODE == DevMode.DESIGN) {
            ScienceEngine.DEV_MODE = DevMode.DEBUG;
          } 
        } else if (keycode == Keys.BACK) {
          goBack();
          return true;
        }
        return super.keyDown(event, keycode);
      }      
    });
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
  
  // Go back one screen in static navigation hierarchy
  protected abstract void goBack();

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
    return scienceEngine.getAtlas();
  }

  protected Table getTable() {
    if (table == null) {
      table = new Table(getSkin());
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
    Gdx.app.log(ScienceEngine.LOG, "Showing screen: " + getName()); //$NON-NLS-1$

    // set the stage as the input processor
    Gdx.input.setInputProcessor(stage);
    // We also treat as back key press
    Gdx.input.setCatchBackKey(true);
    // Catch menu key to prevent onscreen keyboard coming up
    Gdx.input.setCatchMenuKey(true);
  }

  @Override
  public void resize(int width, int height) {
    Gdx.app.log(ScienceEngine.LOG, "Resizing screen: " + getName() +  //$NON-NLS-1$
        " to: " + width + " x " + height); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void render(float delta) {
    stage.act(delta);
    clearScreen(backgroundColor);
    stage.draw();
    Table.drawDebug(stage);
  }

  public void clearScreen(Color color) {
    Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void hide() {
    Gdx.app.log(ScienceEngine.LOG, "Hiding screen: " + getName()); //$NON-NLS-1$

    // dispose the screen when leaving the screen;
    // note that the dipose() method is not called automatically by the
    // framework, so we must figure out when it's appropriate to call it
    dispose();
  }

  @Override
  public void pause() {
    Gdx.app.log(ScienceEngine.LOG, "Pausing screen: " + getName()); //$NON-NLS-1$
  }

  @Override
  public void resume() {
    Gdx.app.log(ScienceEngine.LOG, "Resuming screen: " + getName()); //$NON-NLS-1$
  }

  @Override
  public void dispose() {
    Gdx.app.log(ScienceEngine.LOG, "Disposing screen: " + getName()); //$NON-NLS-1$

    // the following call disposes the screen's stage, but on my computer it
    // crashes the scienceEngine so I commented it out; more info can be found at:
    // http://www.badlogicgames.com/forum/viewtopic.php?f=11&t=3624
    // stage.dispose();

    // as the collaborators are lazily loaded, they may be null
    if (font != null)
      font.dispose();
    if (batch != null)
      batch.dispose();
  }

  public Skin getSkin() {
    return scienceEngine.getSkin();
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }
}
