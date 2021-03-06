package com.mazalearn.scienceengine.app.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.dialogs.AboutDialog;
import com.mazalearn.scienceengine.app.dialogs.UserHomeDialog;
import com.mazalearn.scienceengine.app.services.Profile;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;
import com.mazalearn.scienceengine.app.utils.ScreenUtils;
import com.mazalearn.scienceengine.core.view.CommandClickListener;
import com.mazalearn.scienceengine.core.view.Scoreboard;
import com.mazalearn.scienceengine.core.view.ViewControls;

/**
 * The base class for all scienceEngine screens.
 */
public abstract class AbstractScreen implements Screen {
  protected final ScienceEngine scienceEngine;
  protected Stage stage;

  private Table table;
  private Color backgroundColor = ScienceEngine.getSkin().getColor("background");
  private Profile profile;

  public AbstractScreen(ScienceEngine scienceEngine, Stage stage) {
    this.scienceEngine = scienceEngine;
    this.profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
  }
  
  public String getMsg(String msg) {
    return ScienceEngine.getMsg().getString(msg);
  }
  
  public AbstractScreen(ScienceEngine game) {
    this.scienceEngine = game;
    this.stage = new Stage(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT, false);
    this.profile = ScienceEngine.getPreferencesManager().getActiveUserProfile();
    stage.addListener(new InputListener() {
      @Override
      public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Keys.ALT_LEFT) {
          if (ScienceEngine.DEV_MODE.isDesign()) {
            ScienceEngine.DEV_MODE.setDesign(false);
          } 
        } else if (keycode == Keys.BACK) {
          goBack();
          return true;
        }
        return super.keyDown(event, keycode);
      }      
    });
    if (needsLayout()) {
      setupCoreGroup(stage);
    }
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

  protected Group setupCoreGroup(Stage stage) {
    Group coreGroup = new Group();
    coreGroup.setName(ScreenComponent.CORE_GROUP);
    stage.addActor(coreGroup);
    
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
    return coreGroup;
  }

  private Actor createScreenComponent(ScreenComponent screenComponent, final Stage stage, Skin skin) {
    switch (screenComponent) {
      case TopBar:
      case BottomBar:
        Image bar = new Image(ScreenUtils.createTextureRegion(
            screenComponent.getWidth(), screenComponent.getHeight(), skin.getColor("bar")));
        bar.setName(screenComponent.name());
        return bar;
      case User: {
        String text = ScienceEngine.getUserName();
        TextButton user = new TextButton("", skin, "body");
        user.setName(screenComponent.name());
        final Image image = new Image(ScienceEngine.getTextureRegion(ScienceEngine.USER));
        image.setSize(screenComponent.getWidth(), screenComponent.getHeight());
        user.add(image)
            .width(screenComponent.getWidth())
            .height(screenComponent.getHeight());
        user.add(text);
        user.setSize(screenComponent.getWidth() * 3, screenComponent.getHeight());
        user.addListener(new CommandClickListener() {
          @Override
          public void doCommand() {
            new UserHomeDialog(getSkin(), image).show(stage);
          }      
         });
        return user;
      }
      case Logo:
        Image logo = new Image(new Texture("images/logo.png"));
        logo.setSize(screenComponent.getWidth(), screenComponent.getHeight());
        logo.addListener(new CommandClickListener() {
          @Override
          public void doCommand() {
            new AboutDialog(getSkin()).show(stage);
          }
        });
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
        Drawable imageUp = new TextureRegionDrawable(ScienceEngine.getTextureRegion("back"));
        Drawable imageDown = new TextureRegionDrawable(ScienceEngine.getTextureRegion("backdown"));
        TextButton.TextButtonStyle style = new TextButtonStyle(imageUp, imageDown, imageDown);
        style.font = skin.getFont("default-font");
        backButton.setStyle(style);
        backButton.setName(screenComponent.name());
        backButton.setSize(screenComponent.getWidth(), screenComponent.getHeight());
        backButton.addListener(new CommandClickListener() {
          @Override
          public void doCommand() {
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
      case Scoreboard: {
        Actor actor = new Scoreboard(getSkin());
        actor.setName(screenComponent.name());
        return actor;
      }
    default:
      return null;
    }
  }

  protected Table getTable() {
    if (table == null) {
      table = new Table(getSkin());
      table.setFillParent(true);
      if (ScienceEngine.DEV_MODE.isDebug()) {
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
    Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    stage.draw();
    if (ScienceEngine.DEV_MODE.isDebug()) {
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
    return ScienceEngine.getSkin();
  }

  // Adds all assets required for this screen to reduce load timeLimit
  public void addAssets() {
    // MP3 sound files
    for (ScienceEngineSound sound: ScienceEngineSound.values()) {
      ScienceEngine.getAssetManager().load(sound.getFileName(), Sound.class);
    }
    ScienceEngine.getAssetManager().finishLoading();
  }

  protected Profile getProfile() {
    return profile;
  }
}
