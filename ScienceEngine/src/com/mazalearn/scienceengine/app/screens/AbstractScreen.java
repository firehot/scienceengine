package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.services.IMessage;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.ViewControls;

/**
 * The base class for all scienceEngine screens.
 */
public abstract class AbstractScreen implements Screen {
  protected final ScienceEngine scienceEngine;
  protected Stage stage;

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
    this.stage = new Stage(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT, false);
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

  protected boolean needsLayout() {
    return true;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    setupCoreGroup(stage);
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
  protected void setTitle(String titleString) {
    Actor title = stage.getRoot().findActor(ScreenComponent.Title.name());
    if (title != null) {
      ((Label) title).setText(titleString);
    }
  }

  private void setupCoreGroup(Stage stage) {
    Group coreGroup = (Group) stage.getRoot().findActor(ScreenComponent.CORE_GROUP);
    if (coreGroup == null) {
      coreGroup = new Group();
      coreGroup.setName(ScreenComponent.CORE_GROUP);
      stage.addActor(coreGroup);
    } else if (coreGroup.findActor(ScreenComponent.Title.name()) != null) {
      return;
    }
    // Create core group components
    for (ScreenComponent screenComponent: ScreenComponent.values()) {
      if (!screenComponent.isInAllScreens()) continue;
      Actor component = createScreenComponent(screenComponent, stage, getSkin());
      if (screenComponent.getZIndex() != -1) {
        // Add at the zIndex
        coreGroup.addActorAt(screenComponent.getZIndex(), component);        
      } else {
        coreGroup.addActor(component);
      }
      if ((component instanceof Table) && !(component instanceof Button)) { // Place the center
        Table t = (Table) component;
        float x = screenComponent.getX(t.getPrefWidth()) + t.getPrefWidth() / 2;
        float y = screenComponent.getY(t.getPrefHeight()) + t.getPrefHeight() / 2;
        component.setPosition(x, y);
      } else { // Place the left bottom corner
        component.setPosition(screenComponent.getX(component.getWidth()), 
            screenComponent.getY(component.getHeight()));
      }
    }
  }

  private Actor createScreenComponent(ScreenComponent screenComponent, Stage stage, Skin skin) {
    switch (screenComponent) {
      case TopBar:
      case BottomBar:
        Image bar = new Image(ScreenUtils.createTextureRegion(
            screenComponent.getWidth(), screenComponent.getHeight(), skin.getColor("bar")));
        bar.setName(screenComponent.name());
        return bar;
      case User: { 
        String text = ScienceEngine.getUserName();
        Table table = new Table(skin);
        table.setName(screenComponent.name());
        Image image = new Image(ScienceEngine.getTextureRegion("user"));
        image.setSize(screenComponent.getWidth(), screenComponent.getHeight());
        table.add(image)
            .width(screenComponent.getWidth())
            .height(screenComponent.getHeight());
        table.add(text);
        table.addListener(new ClickListener() {
          public void clicked(InputEvent event, float x, float y) {
            ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
          }      
         });
        return table;
      }
      case Logo:
        Image logo = new Image(new Texture("images/logo.png"));
        return logo;
      case Status: 
      case Title: {
        Table table = new Table(skin);
        Label label = new Label("", skin);
        table.add(label);
        label.setName(screenComponent.name());
        label.setColor(screenComponent.getColor());
        return table;
      }
      case Back: {
        TextButton backButton = 
            new TextButton(ScienceEngine.getMsg().getString("ViewControls.Back"), getSkin()); //$NON-NLS-1$
        Drawable image = new TextureRegionDrawable(ScienceEngine.getTextureRegion("back"));
        TextButton.TextButtonStyle style = new TextButtonStyle(image, image, image);
        style.font = skin.getFont("default-font");
        backButton.setStyle(style);
        backButton.setName(screenComponent.name());
        backButton.setSize(screenComponent.getWidth(), screenComponent.getHeight());
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
    if (needsLayout()) {
      setupCoreGroup(stage);
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
    Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    stage.draw();
    if (ScienceEngine.DEV_MODE == DevMode.DEBUG) {
      Table.drawDebug(stage);
    }
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

  // Adds all assets required for this screen to reduce load timeLimit
  public void addAssets() {
  }

}
