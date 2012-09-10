package com.mazalearn.scienceengine.designer;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectionListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.controller.IModelConfig;
import com.mazalearn.scienceengine.model.IExperimentModel;
import com.mazalearn.scienceengine.screens.AbstractScreen;
import com.mazalearn.scienceengine.services.LevelManager;

/**
 * A supplemental stage that enables editing the screen layout and storing
 * layout in json files. <br/>
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

  private Mode mode = Mode.OVERLAY;
  private ImmediateModeRenderer20 imr;
  private Color fontColor = Color.WHITE;
  private Actor selectedActor, listSelectedActor;
  private Actor draggedActor = null;
  private Actor resizedActor = null;
  private final Vector2 lastTouch = new Vector2();

  private final Stage originalStage;
  private Vector3 originalCameraPos;
  private float originalCameraZoom;
  
  private List componentList;

  private Table levelConfig;
  private IExperimentModel experimentModel;
  private LevelManager levelManager;
  

  /**
   * Build and initialize the editor.
   * @param experimentName - name of experiment.
   * @param level - level of experiment.
   * @param stage - stage used by the experiment view
   */
  public LevelEditor(LevelManager levelManager, Stage stage, 
      IExperimentModel experimentModel, AbstractScreen screen) {
    super(stage.width(), stage.height(), stage.isStretched(), 
        stage.getSpriteBatch());
    this.screen = screen;
    this.experimentModel = experimentModel;
    this.originalStage = stage;
    this.setCamera(stage.getCamera());
    this.orthographicCamera = (OrthographicCamera) this.camera;
    this.levelManager = levelManager;
    levelConfig = new Table(screen.getSkin());
    levelConfig.debug();
    levelConfig.setFillParent(true);
    levelConfig.add(createComponentList(stage, screen.getSkin())).pad(10);
    levelConfig.add(createConfigTable(experimentModel, screen.getSkin())).pad(10);
    this.addActor(levelConfig);
  }

  private List createComponentList(Stage stage, Skin skin) {
    String[] componentNames = new String[stage.getActors().size() + 1];
    int i = 0;
    for (Actor actor: stage.getActors()) {
      if (actor.name != null) {
        componentNames[i++] = actor.name;
      }
    }
    componentNames[i] = "None";
    componentList = new List(componentNames, skin);
    componentList.setSelectionListener(new SelectionListener() {
      @Override
      public void selected(Actor actor, int index, String name) {
        listSelectedActor = originalStage.findActor(name);
      }
    });
    return componentList;
  }

  private Table createConfigTable(IExperimentModel experimentModel, Skin skin) {
    Table configTable = new Table(skin);
    for (final IModelConfig<?> config: experimentModel.getConfigs()) {
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
    return configTable;
  }

  /**
   * Enables the editor. Creates all required resources and replace the current
   * InputProcessor by its own. Just remove this call from your code once you're
   * happy with the result. Any other call can stay without any side-effect.
   */
  public void enableEditor() {
    if (imr == null) {
      imr = new ImmediateModeRenderer20(64, false, true, 0);
    }
    originalCameraZoom = orthographicCamera.zoom;
    originalCameraPos = camera.position.cpy();
    mode = Mode.OVERLAY;
    experimentModel.enable(false);
  }

  private void disableEditor() {
    imr = null;
    restoreCamera();
    screen.setStage(originalStage);
    experimentModel.enable(true);
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
    for (Actor actor : originalStage.getActors()) {
      drawBoundingBox(actor, Color.BLUE);
    }
    if (selectedActor != null) {
      drawBoundingBox(selectedActor, Color.YELLOW);
    }

    int top = Gdx.graphics.getHeight() + 5;
    batch.begin();
    BitmapFont font = screen.getFont();
    font.setColor(fontColor);
    font.draw(batch, "Screen Editor", 5, top - 15 * 0);
    font.draw(batch,
        "---------------------------------------------------------------",
        5, top - 15 * 1);
    font.draw(batch, "'s' to save, 'l' to reload", 5, top - 15 * 2);
    font.draw(batch, "'space' to toggle overlay, 'enter' to exit", 5,
        top - 15 * 3);
    font.draw(batch,
        "scroll to zoom, hold right clic to pan, 'r' to reset camera", 5,
        top - 15 * 4);
    font.draw(batch,
        "---------------------------------------------------------------",
        5, top - 15 * 5);
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

    super.touchDown(x, y, pointer, button);
    switch (button) {
    case Buttons.LEFT:
      // Assumption, stage does not have groups within
      Vector2 stagePoint = new Vector2();
      toStageCoordinates(x, y, stagePoint);
      Vector2 point = new Vector2();
      Vector2 handleSize = screenToWorld(10, -10).sub(screenToWorld(0, 0));
      for (Actor actor : originalStage.getActors()) {
        Group.toChildCoordinates(actor, stagePoint.x, stagePoint.y, point);
        if (actor.hit(point.x, point.y) != null) {
          Vector2 handlePos = new Vector2(actor.width - handleSize.x,
              actor.height - handleSize.y);

          if (handlePos.x <= point.x && point.x <= handlePos.x + handleSize.x
              && handlePos.y <= point.y
              && point.y <= handlePos.y + handleSize.y) {
            resizedActor = actor;
            draggedActor = null;
            selectedActor = actor;
          } else if (resizedActor == null) {
            draggedActor = actor;
            selectedActor = actor;
          }
        }
      }
      break;

    case Buttons.RIGHT:
      break;
    }

    lastTouch.set(x, y);
    return true;
  }

  @Override
  public boolean touchUp(int x, int y, int pointer, int button) {
    super.touchUp(x, y, pointer, button);
    switch (button) {
    case Buttons.LEFT:
      draggedActor = resizedActor = selectedActor = null;
      break;
    }
    selectedActor = listSelectedActor;
    lastTouch.set(x, y);
    return true;
  }

  @Override
  public boolean touchDragged(int x, int y, int pointer) {
    Vector3 delta = new Vector3(x, y, 0);
    camera.unproject(delta);
    Vector3 d3 = new Vector3(lastTouch.x, lastTouch.y, 0);
    camera.unproject(d3);
    delta.sub(d3);

    if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
      if (draggedActor != null) {
        draggedActor.x += delta.x;
        draggedActor.y += delta.y;
        if (draggedActor instanceof Box2DActor) {
          ((Box2DActor) draggedActor).setPositionFromViewCoords();
        }
      } else if (resizedActor != null) {
        float sizeRatio = resizedActor.width / resizedActor.height;
        float originXRatio = resizedActor.originX / resizedActor.width;
        float originYRatio = resizedActor.originY / resizedActor.height;
        resizedActor.width += delta.x;
        resizedActor.height += delta.x / sizeRatio;
        resizedActor.originX = originXRatio * resizedActor.width;
        resizedActor.originY = originYRatio * resizedActor.height;
      }
    }

    if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
      camera.translate(-delta.x, -delta.y, 0);
      camera.update();
    }

    lastTouch.set(x, y);
    return true;
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
    case Keys.SPACE:
      mode = (mode == Mode.CONFIGURE) ? Mode.OVERLAY : Mode.CONFIGURE;
      break;
    case Keys.S:
      levelManager.saveLevel();
      break;
    case Keys.L:
      levelManager.loadLevel();
      break;
    case Keys.V:
      if (selectedActor != null) {
        selectedActor.visible = !selectedActor.visible;
      }
      break;
    case Keys.R:
      restoreCamera();
      break;
    case Keys.ENTER:
      disableEditor();
      break;
    }
    return true;
  }

  private String getInfo(Actor actor) {
    return String.format(Locale.US, "> %s > xy:[%.3f,%.3f] wh:[%.3f,%.3f]",
        actor.name, actor.x, actor.y, actor.width, actor.height);
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

  private void drawBoundingBox(Actor actor, Color color) {
    Vector2 objPos = new Vector2(actor.x + actor.width / 2, actor.y
        + actor.height / 2);
    drawRect(objPos, actor.width, actor.height, color, 2);

    Vector2 handleSize = screenToWorld(10, -10).sub(screenToWorld(0, 0));
    Vector2 handlePos = new Vector2(actor.x + actor.width - handleSize.x / 2,
        actor.y + actor.height - handleSize.y / 2);
    drawRect(handlePos, handleSize.x, handleSize.y, Color.GREEN, 2);
  }

  private void drawRect(Vector2 p, float w, float h, Color c, float lineWidth) {
    Gdx.gl20.glLineWidth(lineWidth);
    imr.begin(batch.getProjectionMatrix(), GL10.GL_LINE_LOOP);
    imr.color(c.r, c.g, c.b, c.a);
    imr.vertex(p.x - w / 2, p.y - h / 2, 0);
    imr.color(c.r, c.g, c.b, c.a);
    imr.vertex(p.x - w / 2, p.y + h / 2, 0);
    imr.color(c.r, c.g, c.b, c.a);
    imr.vertex(p.x + w / 2, p.y + h / 2, 0);
    imr.color(c.r, c.g, c.b, c.a);
    imr.vertex(p.x + w / 2, p.y - h / 2, 0);
    imr.end();
  }
}
