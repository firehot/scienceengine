package com.mazalearn.scienceengine.designer;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.SelectionListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.app.screens.AbstractScreen;
import com.mazalearn.scienceengine.app.services.LevelManager;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
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
  Vector2 point = new Vector2();
  Vector2 rotatedVector = new Vector2();

  private ActorPropertyPanel actorPropertyPanel;

  private Table configTable;
  

  /**
   * Build and initialize the editor.
   * @param controlPanel 
   * @param experimentName - name of experiment.
   * @param level - level of experiment.
   * @param stage - stage used by the experiment view
   */
  public LevelEditor(LevelManager levelManager, ControlPanel controlPanel, Stage stage, 
      IScience2DModel science2DModel, AbstractScreen screen) {
    super(stage.width(), stage.height(), stage.isStretched(), 
        stage.getSpriteBatch());
    this.screen = screen;
    this.science2DModel = science2DModel;
    this.controlPanel = controlPanel;
    this.originalStage = stage;
    this.setCamera(stage.getCamera());
    this.orthographicCamera = (OrthographicCamera) this.camera;
    this.shapeRenderer = new ShapeRenderer();
    this.layout = createLayout(levelManager, stage, science2DModel, screen);
    this.addActor(layout);
  }
  
  private Table createLayout(final LevelManager levelManager, Stage stage,
      IScience2DModel science2DModel, AbstractScreen screen) {
    Table layout = new Table(screen.getSkin());
    layout.setFillParent(true);
    layout.defaults().fill();
    Table titleTable = new Table(screen.getSkin());
    titleTable.defaults().fill();
    titleTable.add(levelManager.getName()).pad(10);
    SelectBox level = new SelectBox(screen.getSkin());
    level.setItems(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
    level.setSelection(levelManager.getLevel() - 1);
    level.setSelectionListener(new SelectionListener() {
      @Override
      public void selected(Actor actor, int index, String value) {
        levelManager.setLevel(index + 1);
        levelManager.load();
      }
    });
    titleTable.add("Level").pad(5);
    titleTable.add(level);
    titleTable.row();
    final TextField description = new TextField(levelManager.getDescription(), screen.getSkin());
    titleTable.add(description).colspan(3).fill();
    description.setTextFieldListener(new TextFieldListener() {
      public void keyTyped(TextField textField, char key) {
        levelManager.setDescription(description.getText());
      }
    });
    
    this.actorPropertyPanel = new ActorPropertyPanel(screen.getSkin(), this);
    configTable = createConfigTable(science2DModel, screen.getSkin());
    Actor componentsPanel = createComponentsPanel(stage, screen.getSkin(), configTable);
    
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
    TextButton button = new TextButton("Save", skin);
    button.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        // Render screen - then we will save level and its screen together
        screen.clearScreen(Color.BLACK);
        originalStage.draw();
        levelManager.save();
      }      
    });
    menu.add(button).pad(10);
    button = new TextButton("Load", skin);
    button.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        levelManager.load();
      }
    });
    menu.add(button).pad(10);
    button = new TextButton("Restore Camera", skin);
    button.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        restoreCamera();
      }
    });
    menu.add(button).pad(10);
    button = new TextButton("Exit Editor", skin);
    button.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        disableEditor();
      }
    });
    menu.add(button).pad(10);
    
    return menu;
  }

  private Actor createComponentsPanel(final Stage stage, final Skin skin, 
      final Table configTable) {
    Table componentsTable= new Table(skin);
    componentsTable.add("Components"); 
    componentsTable.row();
    for (final Actor actor: stage.getActors()) {
      if (actor.name == null || actor == controlPanel) continue;
      final CheckBox componentCheckbox = new CheckBox(actor.name, skin);
      componentsTable.add(componentCheckbox).left();
      componentCheckbox.setChecked(actor.visible);
      componentCheckbox.setClickListener(new ClickListener() {
        @Override
        public void click(Actor a, float x, float y) {
          selectedActor = stage.findActor(actor.name);
          actorPropertyPanel.setActor(selectedActor);
          actor.visible = componentCheckbox.isChecked();
          if (actor instanceof Science2DActor) {
            Science2DActor science2DActor = (Science2DActor) actor;
            science2DActor.getBody().setActive(actor.visible);
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
        configCheckbox.setClickListener(new ClickListener() {
          @Override
          public void click(Actor actor, float x, float y) {
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
  public void enableEditor() {
    originalCameraZoom = orthographicCamera.zoom;
    originalCameraPos = camera.position.cpy();
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
    camera.update();
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    root.draw(batch, 1);
    batch.end();
  }

  @Override
  public boolean touchDown(int x, int y, int pointer, int button) {
    lastTouch.set(x,y);
    if (mode == Mode.CONFIGURE || button != Buttons.LEFT) {
      return super.touchDown(x, y, pointer, button);
    }
    
    // Assumption, stage does not have groups within
    Vector2 stagePoint = new Vector2();
    toStageCoordinates(x, y, stagePoint);
    Vector2 handleSize = screenToWorld(10, -10).sub(screenToWorld(0, 0));
    for (Actor actor : originalStage.getActors()) {
      Group.toChildCoordinates(actor, stagePoint.x, stagePoint.y, point);
      Actor child = actor.hit(point.x, point.y);
      if (child != null) {
        if (isAncestor(controlPanel, actor) && child != controlPanel.getTitle()) {
          originalStage.touchDown(x, y, pointer, button);
          return true;
        } else {
          Vector2 handlePos = new Vector2(actor.width - handleSize.x,
              actor.height - handleSize.y);

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

  private boolean isAncestor(Actor actor1, Actor actor2) {
    if (actor1 == actor2) return true;
    if (actor2 == null) return false;
    return isAncestor(actor1, actor2.parent);
  }

  @Override
  public boolean touchUp(int x, int y, int pointer, int button) {
    super.touchUp(x, y, pointer, button);
    
    toStageCoordinates(x, y, point);
    Group.toChildCoordinates(controlPanel, point.x, point.y, point);
    Actor child = controlPanel.hit(point.x, point.y);
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
    camera.unproject(delta3);
    Vector3 d3 = new Vector3(lastTouch.x, lastTouch.y, 0);
    camera.unproject(d3);
    delta3.sub(d3);

    if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
      toStageCoordinates(x, y, point);
      Group.toChildCoordinates(controlPanel, point.x, point.y, point);
      Actor child = controlPanel.hit(point.x, point.y);
      if (child != null && child != controlPanel.getTitle()) {
        originalStage.touchDragged(x, y, pointer);
        lastTouch.set(x, y);
        return true;
      }
      operateOnActor(selectedActor, x, y, delta3);
    }

    if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
      camera.translate(-delta3.x, -delta3.y, 0);
      camera.update();
    }

    lastTouch.set(x, y);
    return true;
  }

  private void operateOnActor(Actor actor, int x, int y, Vector3 delta) {
    if (operation == null || actor == null) return;
    
    switch (operation) {
    case MOVE:
      actor.x += delta.x;
      actor.y += delta.y;
      break;
    case ROTATE:
      toStageCoordinates(x, y, point);
      Group.toChildCoordinates(actor, point.x, point.y, rotatedVector);
      rotatedVector.sub(actor.width, 0);
      // TODO: UI issues and dont know how to draw rotated rectangles
      // selectedActor.rotation = rotatedVector.angle();
      break;
    case RESIZE:
      float sizeRatio = actor.width / actor.height;
      float originXRatio = actor.originX / actor.width;
      float originYRatio = actor.originY / actor.height;
      actor.width += delta.x;
      actor.height += delta.x / sizeRatio;
      actor.originX = originXRatio * actor.width;
      actor.originY = originYRatio * actor.height;
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
    camera.update();
    return true;
  }

  @Override
  public boolean keyDown(int keycode) {
    switch (keycode) {
    case Keys.ALT_LEFT:
      mode = (mode == Mode.CONFIGURE) ? Mode.OVERLAY : Mode.CONFIGURE;
      break;
    case Keys.TAB: // Cycle to next actor if selected Actor not null
      java.util.List<Actor> actors = originalStage.getActors();
      for (int i = 0; i < actors.size(); i++) {
        Actor actor = actors.get(i);
        if (selectedActor == actor) {
          selectedActor = actors.get((i + 1) % actors.size());
          break;
        }
      }
      if (selectedActor == null) selectedActor = actors.get(0);
      break;
    }
    return true;
  }

  private String getInfo(Actor actor) {
    return String.format(Locale.US, "> %s %s > xy:[%.3f,%.3f] wh:[%.3f,%.3f] rot:%.3f",
        actor.name, 
        operation == null ? "selected" : operation.name(),
        actor.x, actor.y, actor.width, actor.height, actor.rotation);
  }

  private void restoreCamera() {
    orthographicCamera.zoom = originalCameraZoom;
    camera.position.set(originalCameraPos);
    camera.update();
  }

  private Vector2 screenToWorld(int x, int y) {
    Vector3 v3 = new Vector3(x, y, 0);
    camera.unproject(v3);
    return new Vector2(v3.x, v3.y);
  }

  private void drawBoundingBox(Actor actor, boolean selected) {
    // Draw outline for actor
    shapeRenderer.begin(ShapeType.Rectangle);
    shapeRenderer.setColor(selected ? Color.YELLOW : Color.BLUE);
    if (actor == controlPanel) {
      // Bounding box only for status cell
      actor = controlPanel.getTitle();
    }
    shapeRenderer.rect(actor.x, actor.y, actor.width, actor.height);

    // Draw handle for changing the size of the actor
    Vector2 handleSize = screenToWorld(10, -10).sub(screenToWorld(0, 0));
    shapeRenderer.setColor(Color.GREEN);
    shapeRenderer.rect(actor.x + actor.width - handleSize.x, 
        actor.y + actor.height - handleSize.y, handleSize.x, handleSize.y);
    shapeRenderer.end();
    
    // Draw handle for rotation
    shapeRenderer.begin(ShapeType.FilledRectangle);
    shapeRenderer.setColor(Color.GREEN);
    shapeRenderer.filledRect(actor.x + actor.width - handleSize.x, actor.y, 
        handleSize.x, handleSize.y);
    shapeRenderer.end();
    
    // Draw origin
    shapeRenderer.begin(ShapeType.FilledCircle);
    shapeRenderer.setColor(Color.GREEN);
    shapeRenderer.filledCircle(actor.x + actor.originX, 
        actor.y + actor.originY, handleSize.x / 2);
    shapeRenderer.end();
  }
}
