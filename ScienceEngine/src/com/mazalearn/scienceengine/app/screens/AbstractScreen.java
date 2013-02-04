package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.core.view.ViewControls;
import com.mazalearn.scienceengine.guru.IDoneCallback;

/**
 * The base class for all scienceEngine screens.
 */
public abstract class AbstractScreen implements Screen {
  public static final int VIEWPORT_WIDTH = 800,
      VIEWPORT_HEIGHT = 480;

  protected final ScienceEngine scienceEngine;
  protected Stage stage;

  private static BitmapFont font;
  private Table table;
  private Color backgroundColor = Color.BLACK;

  public AbstractScreen(ScienceEngine scienceEngine, Stage stage) {
    this.scienceEngine = scienceEngine;
  }
  
  public IMessage getMsg() {
    return ScienceEngine.getMsg();
  }
  
  public AbstractScreen(ScienceEngine game) {
    this.scienceEngine = game;
    this.stage = new Stage(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, false);
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

  protected boolean needsBackground() {
    return true;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    setupScreen(stage);
    Gdx.input.setInputProcessor(stage);    
  }

  public Stage getStage() {
    return stage;
  }
  
  protected String getName() {
    return getClass().getName();
  }

  // Go back one screen in static navigation hierarchy
  protected abstract void goBack();

  // Lazily loaded collaborators

  public BitmapFont getSmallFont() {
    if (font == null) {
      font = ScienceEngine.getPlatformAdapter().getScaledFont(10);
    }
    return font;
  }

  public TextureAtlas getAtlas() {
    return scienceEngine.getAtlas();
  }

  protected void setTitle(String titleString) {
    Actor title = stage.getRoot().findActor(ScreenComponent.Title.name());
    if (title != null) {
      ((Label) title).setText(titleString);
    }
  }

  private void setupBackground(Stage stage) {
    setBackgroundColor(Color.CLEAR);
    // retrieve the splash image's region from the atlas
    AtlasRegion background = getAtlas().findRegion(
        "splash-screen/splash-background"); //$NON-NLS-1$
    Image bgImage = new Image(background);
    bgImage.setSize(AbstractScreen.VIEWPORT_WIDTH, AbstractScreen.VIEWPORT_HEIGHT);
    // Background should be behind everything else on stage.
    if (stage.getActors().size > 0) {
      stage.getRoot().addActorBefore(stage.getActors().get(0), bgImage);
    } else {
      stage.addActor(bgImage);
    }
  }

  public void setupScreen(Stage stage) {
    if (stage.getRoot().findActor(ScreenComponent.Title.name()) != null) return;
    setupBackground(stage);
    setupScreenComponents(stage);
  }
  
  private void setupScreenComponents(Stage stage) {
    // Register stage components
    for (ScreenComponent screenComponent: ScreenComponent.values()) {
      Actor component = addScreenComponent(screenComponent, stage, getSkin());
      stage.addActor(component);
      float x = screenComponent.getX();
      if (x < 0) {
        x = AbstractScreen.VIEWPORT_WIDTH + x;
      }
      float y = screenComponent.getY();
      if (y < 0) {
        y = AbstractScreen.VIEWPORT_HEIGHT + y;
      }
      component.setPosition(x, y);
    }
  }

  private Actor addScreenComponent(ScreenComponent screenComponent, Stage stage, Skin skin) {
    switch (screenComponent) { 
      case User: { 
        String text = ScienceEngine.getUserName();
        Table table = new Table(skin);
        table.setName(screenComponent.name());
        Image image = new Image(new Texture("images/user.png"));
        image.setSize(30,30);
        table.add(image).width(20).height(30);
        table.add(text);
        table.addListener(new ClickListener() {
          public void clicked(InputEvent event, float x, float y) {
            ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
            new LoginDialog(getSkin(), new IDoneCallback() {
              @Override public void done(boolean success) {}
            }).show(getStage());
          }      
         });
        return table;
      }
      case Status: 
      case Title: {
        Table table = new Table(skin);
        Label label = new Label("", skin);
        table.add(label);
        label.setName(screenComponent.name());
        label.setColor(screenComponent.getColor());
        return table;
      }
      case BackButton: {
        TextButton backButton = 
            new TextButton(ScienceEngine.getMsg().getString("ViewControls.Back"), getSkin()); //$NON-NLS-1$
        Drawable image = new TextureRegionDrawable(new TextureRegion(new Texture("images/back.png")));
        TextButton.TextButtonStyle style = new TextButtonStyle(image, image, image);
        style.font = skin.getFont("default-font");
        backButton.setStyle(style);
        backButton.setName(screenComponent.name());
        backButton.setSize(70, 30);
        backButton.addListener(new ClickListener() {
          public void clicked(InputEvent event, float x, float y) {
            ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
            goBack();
          }      
        });
        return backButton;
      }
      case ViewControls: {
        Actor actor = stage.getRoot().findActor(ScreenComponent.ViewControls.name());
        if (actor == null) {
          ViewControls viewControls = new ViewControls(getSkin());
          viewControls.addActivityControls();
          actor = viewControls;
        }
        return actor;
      }
    }
    return null;
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
    if (needsBackground()) {
      setupScreen(stage);
    }
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
    if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
      Table.drawDebug(stage);
    }
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

  // Adds all assets required for this screen to reduce load timeLimit
  public void addAssets() {
  }

}
