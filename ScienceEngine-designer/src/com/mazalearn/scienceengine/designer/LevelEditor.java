package com.mazalearn.scienceengine.designer;

import java.io.IOException;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.view.IScience2DStage;
import com.mazalearn.scienceengine.core.view.Science2DActor;
import com.mazalearn.scienceengine.experiments.ControlPanel;

/**
 * A designer that enables editing the screen layout, configurations available
 *  and storing layout and configurations in a json file for that level. <br/>
 * <br/>
 * 
 * It takes over all inputs and also allows zoom and pan of the
 * scene to easily adjust the components. <br/>
 * <br/>
 *  
 * @author sridhar
 */
public class LevelEditor extends Stage {
  private final AbstractScreen screen;

  private final OrthographicCamera orthographicCamera;
  enum Mode {
    CONFIGURE, OVERLAY
  };
  
  enum Operation {
    MOVE, ROTATE, RESIZE
  };

  private Operation operation = null;
  private Mode mode = Mode.OVERLAY;
  private Color fontColor = Color.WHITE;
  private Actor selectedActor;
  private final Vector2 lastTouch = new Vector2();

  private final Stage originalStage;
  private Vector3 originalCameraPos;
  private float originalCameraZoom;
  
  private Table layout;
  private IScience2DModel science2DModel;
  private ControlPanel controlPanel;
  private ShapeRenderer shapeRenderer;
  private final Vector2 stagePoint = new Vector2();;
  private final Vector2 point = new Vector2();
  private final Vector2 rotatedVector = new Vector2();

  private ActorPropertyPanel actorPropertyPanel;
  private Table configTable;

  

  /**
   * Build and initialize the editor.
   * @param controlPanel 
   * @param experimentName - name of experiment.
   * @param level - level of experiment.
   * @param stage - stage used by the experiment view
   */
  public LevelEditor(IScience2DController controller, AbstractScreen screen) {
    super(((Stage)controller.getView()).getWidth(), 
        ((Stage) controller.getView()).getHeight(), 
        false, 
        ((Stage)controller.getView()).getSpriteBatch());
    this.screen = screen;
    this.science2DModel = controller.getModel();
    this.controlPanel = controller.getControlPanel();
    this.originalStage = (Stage) controller.getView();
    this.setCamera(originalStage.getCamera());
    this.orthographicCamera = (OrthographicCamera) this.getCamera();
    this.shapeRenderer = new ShapeRenderer();
    this.layout = createLayout(controller.getView().getLevelManager(),
        originalStage, science2DModel);
    this.addActor(layout);
    this.enableEditor();
  }
  
  private Table createLayout(final LevelManager levelManager, Stage stage,
      IScience2DModel science2DModel) {
    Table layout = new Table(screen.getSkin());
    layout.setName("Layout");
    layout.setFillParent(true);
    layout.defaults().fill();
    Table titleTable = new Table(screen.getSkin());
    titleTable.setName("Title");
    titleTable.defaults().fill();
    titleTable.add(levelManager.getName()).pad(10);
    final SelectBox level = 
        new SelectBox(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, 
            screen.getSkin());
    level.setSelection(levelManager.getLevel());
    level.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        levelManager.setLevel(level.getSelectionIndex() + 1);
        levelManager.load();
      }
    });
    titleTable.add("Level").pad(5);
    titleTable.add(level);
    titleTable.row();
    final Label titleLabel = (Label) originalStage.getRoot().findActor("Title");
    final TextField description = 
        new TextField((String) titleLabel.getText(), screen.getSkin());
    titleTable.add(description).colspan(3).fill().width(600);
    description.setTextFieldListener(new TextFieldListener() {
      public void keyTyped(TextField textField, char key) {
        titleLabel.setText(description.getText());
      }
    });
    
    this.actorPropertyPanel = new ActorPropertyPanel(screen.getSkin(), this);
    configTable = createConfigTable(science2DModel, screen.getSkin());
    Actor componentsPanel = 
        createComponentsPanel(stage, screen.getSkin(), configTable);
    
    Table menu = createMenu(levelManager, screen.getSkin());

    layout.add(titleTable).colspan(3);
    layout.row();
    layout.add(componentsPanel).top().pad(10).width(150).fill();
    layout.add(actorPropertyPanel).top().pad(10).fill().width(200);
    layout.add(configTable).top().pad(10).width(300).right().fill();
    layout.row();
    layout.add(menu).colspan(3);
    layout.row();
    return layout;
  }

  private Table createMenu(final LevelManager levelManager, final Skin skin) {
    Table menu = new Table(skin);
    menu.setName("Menu");
    TextButton button = new TextButton("Save", skin);
    button.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        // Render screen - then we will save level and its screen together
        screen.clearScreen(Color.BLACK);
        originalStage.draw();
        try {
          LevelSaver levelSaver = new LevelSaver(
              levelManager.getLevel(), 
              controlPanel, (IScience2DStage) originalStage, science2DModel);
          levelSaver.save();
          System.out.println("[LevelEditor] Level successfully saved!");
        } catch (IOException ex) {
          System.err.println("[LevelEditor] Error happened while writing level file");
        }
      }      
    });
    menu.add(button).pad(10);
    button = new TextButton("Load", skin);
    button.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        levelManager.load();
      }
    });
    menu.add(button).pad(10);
    button = new TextButton("Restore Camera", skin);
    button.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        restoreCamera();
      }
    });
    menu.add(button).pad(10);
    button = new TextButton("Exit Editor", skin);
    button.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        disableEditor();
      }
    });
    menu.add(button).pad(10);
    
    return menu;
  }

  private Actor createComponentsPanel(final Stage stage, final Skin skin, 
      final Table configTable) {
    Table componentsTable= new Table(skin);
    componentsTable.setName("Components");
    componentsTable.add("Components"); 
    componentsTable.row();
    for (final Actor actor: stage.getActors()) {
      if (actor.getName() == null || actor == controlPanel) continue;
      final CheckBox componentCheckbox = new CheckBox(actor.getName(), skin);
      componentsTable.add(componentCheckbox).left();
      componentCheckbox.setChecked(actor.isVisible());
      componentCheckbox.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          selectedActor = stage.getRoot().findActor(actor.getName());
          actorPropertyPanel.setActor(selectedActor);
          actor.setVisible(componentCheckbox.isChecked());
          if (actor instanceof Science2DActor) {
            Science2DActor science2DActor = (Science2DActor) actor;
            science2DActor.getBody().setActive(actor.isVisible());
            refreshConfigsTable(science2DModel, skin, configTable);
            controlPanel.refresh();
         }
        }});
      componentsTable.row();
    }
    return componentsTable;
  }

  private Table createConfigTable(IScience2DModel science2DModel, Skin skin) {
    Table configTable = new Table(skin);
    configTable.setName("Configs");
    refreshConfigsTable(science2DModel, skin, configTable);
    return configTable;
  }

  private void refreshConfigsTable(IScience2DModel science2DModel, Skin skin,
      Table configTable) {
    configTable.clear();
    configTable.add("Configs");
    configTable.row();
    for (final IModelConfig<?> config: science2DModel.getAllConfigs()) {
      if (config.isPossible()) {
        final CheckBox configCheckbox = new CheckBox(config.getName(), skin);
        configTable.add(configCheckbox).left();
        configCheckbox.setChecked(config.isPermitted());
        configCheckbox.addListener(new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            config.setPermitted(configCheckbox.isChecked());
          }
        });
        configTable.row();
      }
    }
  }

  /**
   * Enables the editor. Creates all required resources and replace the current
   * InputProcessor by its own. Just remove this call from your code once you're
   * happy with the result. Any other call can stay without any side-effect.
   */
  private void enableEditor() {
    originalCameraZoom = orthographicCamera.zoom;
    originalCameraPos = getCamera().position.cpy();
    mode = Mode.OVERLAY;
    science2DModel.enable(false);
  }

  private void disableEditor() {
    restoreCamera();
    screen.setStage(originalStage);
    science2DModel.enable(true);
  }

  /**
   * Renders the editor overlay.
   */
  @Override
  public void draw() {
    switch (mode) {
    case OVERLAY:
      originalStage.draw();
      Table.drawDebug(originalStage);
      drawOverlay();
      break;
    case CONFIGURE:
      drawConfigure();
      break;
    }
  }

  private void drawOverlay() {
    shapeRenderer.setProjectionMatrix(originalStage.getCamera().combined);

    for (Actor actor : originalStage.getActors()) {
      drawBoundingBox(actor, actor == selectedActor);
    }
 
    int top = Gdx.graphics.getHeight() + 5;
    SpriteBatch batch = getSpriteBatch();
    batch.begin();
    BitmapFont font = screen.getFont();
    font.setColor(fontColor);
    font.draw(batch, "LEVEL Editor", 5, top - 15 * 0);
    font.draw(batch,
        "---------------------------------------------------------------",
        5, top - 15 * 1);
    font.draw(batch, "'Alt' to toggle overlay, 'Tab' to select", 5,
        top - 15 * 2);
    font.draw(batch,
        "---------------------------------------------------------------",
        5, top - 15 * 3);
    font.draw(batch, selectedActor != null ? getInfo(selectedActor)
        : "> No object selected", 5, top - 15 * 6);
    batch.end();
  }

  private void drawConfigure() {
    getCamera().update();
    getSpriteBatch().setProjectionMatrix(getCamera().combined);
    getSpriteBatch().begin();
    getRoot().draw(getSpriteBatch(), 1);
    getSpriteBatch().end();
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    lastTouch.set(x,y);
    if (mode == Mode.CONFIGURE || button != Buttons.LEFT) {
      return super.touchDown(x, y, pointer, button);
    }
    
    screenToStageCoordinates(stagePoint.set(x, y));
    Vector2 handleSize = screenToWorld(10, -10).sub(screenToWorld(0, 0));
    for (Actor actor : originalStage.getActors()) {
      actor.stageToLocalCoordinates(point.set(stagePoint));
      Actor child = actor.hit(point.x, point.y, true);
      if (child != null) {
        if (controlPanel.isAscendantOf(actor) && child != controlPanel.getTitle()) {
          originalStage.touchDown(x, y, pointer, button);
          return true;
        } else {
          Vector2 handlePos = new Vector2(actor.getWidth() - handleSize.x,
              actor.getHeight() - handleSize.y);

          // Right top corner handle
          if (handlePos.x <= point.x && point.x <= handlePos.x + handleSize.x
              && handlePos.y <= point.y
              && point.y <= handlePos.y + handleSize.y) {
            operation = Operation.RESIZE;
          } else if (0 <= point.y && point.y <= handlePos.y && 
              handlePos.x <= point.x && point.x <= handlePos.x + handleSize.x) {
            // Right bottom corner handle
            operation = Operation.ROTATE;
          } else {
            operation = Operation.MOVE;
          }
          selectedActor = actor;
          actorPropertyPanel.setActor(selectedActor);
        }
      }
    }

    return true;
  }

  @Override
  public boolean touchUp(int x, int y, int pointer, int button) {
    super.touchUp(x, y, pointer, button);
    
    screenToStageCoordinates(point.set(x, y));
    controlPanel.stageToLocalCoordinates(point);
    Actor child = controlPanel.hit(point.x, point.y, true);
    if (child != null && child != controlPanel.getTitle()) {
      originalStage.touchUp(x, y, pointer, button);
      lastTouch.set(x, y);
      return true;
    }
    
    switch (button) {
    case Buttons.LEFT:
      operation = null;
      break;
    }
    lastTouch.set(x, y);
    return true;
  }

  @Override
  public boolean touchDragged(int x, int y, int pointer) {
    Vector3 delta3 = new Vector3(x, y, 0);
    getCamera().unproject(delta3);
    Vector3 d3 = new Vector3(lastTouch.x, lastTouch.y, 0);
    getCamera().unproject(d3);
    delta3.sub(d3);

    if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
      screenToStageCoordinates(point.set(x, y));
      controlPanel.stageToLocalCoordinates(point);
      Actor child = controlPanel.hit(point.x, point.y, true);
      if (child != null && child != controlPanel.getTitle()) {
        originalStage.touchDragged(x, y, pointer);
        lastTouch.set(x, y);
        return true;
      }
      operateOnActor(selectedActor, x, y, delta3);
    }

    if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
      getCamera().translate(-delta3.x, -delta3.y, 0);
      getCamera().update();
    }

    lastTouch.set(x, y);
    return true;
  }

  private void operateOnActor(Actor actor, int x, int y, Vector3 delta) {
    if (operation == null || actor == null) return;
    
    switch (operation) {
    case MOVE:
      actor.setX(actor.getX() + delta.x);
      actor.setY(actor.getY() + delta.y);
      break;
    case ROTATE:
      screenToStageCoordinates(rotatedVector.set(x, y));
      actor.stageToLocalCoordinates(rotatedVector);
      rotatedVector.sub(actor.getWidth(), 0);
      // TODO: UI issues and dont know how to draw rotated rectangles
      // selectedActor.rotation = rotatedVector.angle();
      break;
    case RESIZE:
      float sizeRatio = actor.getWidth() / actor.getHeight();
      float originXRatio = actor.getOriginX() / actor.getWidth();
      float originYRatio = actor.getOriginY() / actor.getHeight();
      actor.setWidth(actor.getWidth() + delta.x);
      actor.setHeight(actor.getHeight() + (delta.x / sizeRatio));
      actor.setOriginX(originXRatio * actor.getWidth());
      actor.setOriginY(originYRatio * actor.getHeight());
      break;
    }
    // Refresh actor properties
    actorPropertyPanel.setActor(selectedActor);
    if (actor instanceof Science2DActor) {
      // This is a user initiated move but for editing we want the 
      // actors in the location group to be individually moved.
      ((Science2DActor) actor).setPositionFromViewCoords(false);
    }
  }

  @Override
  public boolean scrolled(int amount) {
    orthographicCamera.zoom *= amount > 0 ? 1.2f : 1 / 1.2f;
    getCamera().update();
    return true;
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
    case Keys.ALT_LEFT:
      mode = (mode == Mode.CONFIGURE) ? Mode.OVERLAY : Mode.CONFIGURE;
      break;
    case Keys.TAB: // Cycle to next actor if selected Actor not null
      Array<Actor> actors = originalStage.getActors();
      for (int i = 0; i < actors.size; i++) {
        Actor actor = actors.get(i);
        if (selectedActor == actor) {
          selectedActor = actors.get((i + 1) % actors.size);
          break;
        }
      }
      if (selectedActor == null) selectedActor = actors.get(0);
      actorPropertyPanel.setActor(selectedActor);
      break;
    case Keys.ESCAPE: // Clear selected actor
      selectedActor = null;
      actorPropertyPanel.setActor(selectedActor);
      break;
    }
    return true;
  }

  private String getInfo(Actor actor) {
    return String.format(Locale.US, "> %s %s > xy:[%.3f,%.3f] wh:[%.3f,%.3f] rot:%.3f",
        actor.getName(), 
        operation == null ? "selected" : operation.name(),
        actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight(), actor.getRotation());
  }

  private void restoreCamera() {
    orthographicCamera.zoom = originalCameraZoom;
    getCamera().position.set(originalCameraPos);
    getCamera().update();
  }

  private Vector2 screenToWorld(int x, int y) {
    Vector3 v3 = new Vector3(x, y, 0);
    getCamera().unproject(v3);
    return new Vector2(v3.x, v3.y);
  }

  private void drawBoundingBox(Actor actor, boolean selected) {
    // Draw outline for actor
    shapeRenderer.begin(ShapeType.Rectangle);
    shapeRenderer.setColor(selected ? Color.YELLOW : Color.BLUE);
    if (actor == controlPanel) {
      // Bounding box only for status cell
      Actor title = controlPanel.getTitle();
      shapeRenderer.rect(actor.getX() - controlPanel.getPrefWidth()/2, 
          actor.getY() + controlPanel.getPrefHeight()/2 - title.getHeight(), 
          title.getWidth(), title.getHeight());
      shapeRenderer.end();
      return;
    }
    shapeRenderer.rect(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());

    // Draw handle for changing the size of the actor
    Vector2 handleSize = screenToWorld(10, -10).sub(screenToWorld(0, 0));
    shapeRenderer.setColor(Color.GREEN);
    shapeRenderer.rect(actor.getX() + actor.getWidth() - handleSize.x, 
        actor.getY() + actor.getHeight() - handleSize.y, handleSize.x, handleSize.y);
    shapeRenderer.end();
    
    // Draw handle for rotation
    shapeRenderer.begin(ShapeType.FilledRectangle);
    shapeRenderer.setColor(Color.GREEN);
    shapeRenderer.filledRect(actor.getX() + actor.getWidth() - handleSize.x, actor.getY(), 
        handleSize.x, handleSize.y);
    shapeRenderer.end();
    
    // Draw origin
    shapeRenderer.begin(ShapeType.FilledCircle);
    shapeRenderer.setColor(Color.GREEN);
    shapeRenderer.filledCircle(actor.getX() + actor.getOriginX(), 
        actor.getY() + actor.getOriginY(), handleSize.x / 2);
    shapeRenderer.end();
  }
}