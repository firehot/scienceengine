package com.mazalearn.scienceengine.designer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
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
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectionListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.ScienceEngine.DevMode;
import com.mazalearn.scienceengine.box2d.Box2DActor;

/**
 * A supplemental stage that enables editing the screen layout and storing
 * layout in json files. <br/>
 * <br/>
 * 
 * When enabled, it takes over all inputs and also allows zoom and pan of the
 * scene to easily adjust the components. <br/>
 * <br/>
 * 
 * Usage is as follows:<br/>
 * 1. Create the ScreenEditor with the stage, its name and level.<br/>
 * 3. Call load() method to import previously saved stuff, if any.<br/>
 * 4. Call enable() method if you want to enable the editor to make changes to
 * your scene.<br/>
 * 5. Call draw() in your rendering loop to show the overlay.<br/>
 * <br/>
 * 
 * <b>Note that the everything you need to remove once you don't want to make
 * anymore changes is the call to enable(). </b>
 * 
 * @author sridhar
 */
public class ScreenEditor extends Stage {
  private final FileHandle file;

  private final OrthographicCamera orthographicCamera;
  private boolean isEnabled = false;

  enum OverlayMode {
    NO_OVERLAY, OVERLAY_NO_HELP, OVERLAY_WITH_HELP
  };

  private OverlayMode overlayMode = OverlayMode.OVERLAY_WITH_HELP;
  private ImmediateModeRenderer20 imr;
  private BitmapFont font;
  private Color fontColor = Color.WHITE;
  private Actor selectedActor, listSelectedActor;
  private Actor draggedActor = null;
  private Actor resizedActor = null;
  private final Vector2 lastTouch = new Vector2();

  private final Stage originalStage;
  private Vector3 originalCameraPos;
  private float originalCameraZoom;
  private InputProcessor originalInputProcessor;

  private List componentList;

  /**
   * Build and initialize the editor.
   * @param experimentName - name of experiment.
   * @param level - level of experiment.
   * @param stage - stage used by the experiment view
   */
  public ScreenEditor(String experimentName, int level, Stage stage, BitmapFont font, Skin skin) {
    super(stage.width(), stage.height(), stage.isStretched(), 
        stage.getSpriteBatch());
    this.file = Gdx.files.internal("data/" + experimentName + "." + level + ".json");
    this.originalStage = stage;
    this.setCamera(stage.getCamera());
    this.orthographicCamera = (OrthographicCamera) this.camera;
    this.font = font;
    setupComponentList(stage, skin);
    loadLevel();
  }

  private void setupComponentList(Stage stage, Skin skin) {
    String[] names = new String[stage.getActors().size() + 1];
    int i = 0;
    for (Actor actor: stage.getActors()) {
      if (actor.name != null)
        names[i++] = actor.name;
    }
    names[i] = "None";
    componentList = new List(names, skin);
    componentList.setSelectionListener(new SelectionListener() {
      @Override
      public void selected(Actor actor, int index, String name) {
        listSelectedActor = originalStage.findActor(name);
      }
    });
    this.addActor(componentList);
  }

  /**
   * Loads the content of the provided file and automatically position and size
   * the objects.
   */
  public void loadLevel() {
    try {
      loadFile();
      System.out.println("[ScreenEditor] File successfully loaded!");
    } catch (GdxRuntimeException ex) {
      System.err.println("[ScreenEditor] Error happened while loading "
          + file.path());
    }
  }

  /**
   * Enables the editor. Creates all required resources and replace the current
   * InputProcessor by its own. Just remove this call from your code once you're
   * happy with the result. Any other call can stay without any side-effect.
   */
  public void enable() {
    if (ScienceEngine.DEV_MODE == DevMode.PRODUCTION)
      return;
    if (imr == null) {
      imr = new ImmediateModeRenderer20(64, false, true, 0);
    }
    originalCameraZoom = orthographicCamera.zoom;
    originalCameraPos = camera.position.cpy();
    originalInputProcessor = Gdx.input.getInputProcessor();
    Gdx.input.setInputProcessor(this);
    this.root.visible = true;
    isEnabled = true;
  }

  /**
   * Renders the editor overlay.
   */
  @Override
  public void draw() {
    camera.update();
    if (!root.visible)
      return;
    batch.setProjectionMatrix(camera.combined);
    batch.begin();
    root.draw(batch, 1);
    batch.end();
    if (isEnabled && overlayMode != OverlayMode.NO_OVERLAY) {
      for (Actor actor : originalStage.getActors()) {
        drawBoundingBox(actor, Color.BLUE);
      }
      if (selectedActor != null) {
        drawBoundingBox(selectedActor, Color.YELLOW);
      }

      int top = Gdx.graphics.getHeight() + 5;
      switch (overlayMode) {
      case OVERLAY_WITH_HELP:
        batch.begin();
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
        break;

      case OVERLAY_NO_HELP:
        batch.begin();
        font.setColor(fontColor);
        font.draw(batch, "Screen Editor", 5, top - 15 * 0);
        font.draw(batch,
            "---------------------------------------------------------------",
            5, top - 15 * 1);
        font.draw(batch, selectedActor != null ? getInfo(selectedActor)
            : "> No object selected", 5, top - 15 * 2);
        batch.end();
        break;
      }
    }
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
    Vector2 delta = new Vector2(x, y).sub(lastTouch).mul(
        orthographicCamera.zoom);
    delta.x *= camera.viewportWidth / Gdx.graphics.getWidth();
    delta.y *= camera.viewportHeight / Gdx.graphics.getHeight();
    delta.y = -delta.y;

    if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
      if (draggedActor != null) {
        draggedActor.x += delta.x;
        draggedActor.y += delta.y;
        if (draggedActor instanceof Box2DActor) {
          ((Box2DActor) draggedActor).setPositionFromScreen();
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
      switch (overlayMode) {
      case NO_OVERLAY:
        overlayMode = OverlayMode.OVERLAY_NO_HELP;
        break;
      case OVERLAY_NO_HELP:
        overlayMode = OverlayMode.OVERLAY_WITH_HELP;
        break;
      case OVERLAY_WITH_HELP:
        overlayMode = OverlayMode.NO_OVERLAY;
        break;
      }
      break;
    case Keys.S:
      saveLevel();
      break;
    case Keys.L:
      loadLevel();
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
      disable();
      break;
    }
    return true;
  }

  private void saveLevel() {
    try {
      writeFile();
      System.out.println("[ScreenEditor] File successfully saved!");
    } catch (IOException ex) {
      System.err.println("[ScreenEditor] Error happened while writing "
          + file.path());
    }
  }

  private void disable() {
    if (isEnabled) {
      imr = null;
      restoreCamera();
      Gdx.input.setInputProcessor(originalInputProcessor);
      isEnabled = false;
      this.root.visible = false;
    }
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

  private void writeFile() throws IOException {
    FileWriter writer = new FileWriter(file.file());
    JsonWriter jsonWriter = new JsonWriter(writer);
    jsonWriter = jsonWriter.object().array("components");
    for (Actor a : originalStage.getActors()) {
      jsonWriter.object()
          .set("name", a.name)
          .set("x", a.x)
          .set("y", a.y)
          .set("originX", a.originX)
          .set("originY", a.originY)
          .set("width", a.width)
          .set("height", a.height)
          .set("visible", a.visible)
          .pop();
    }
    jsonWriter.flush();
    jsonWriter.close();
  }

  @SuppressWarnings("unchecked")
  private void loadFile() {
    String str = file.readString();
    OrderedMap<String, ?> rootElem = (OrderedMap<String, ?>) new JsonReader()
        .parse(str);

    Array<?> components = (Array<?>) rootElem.get("components");
    for (int i = 0; i < components.size; i++) {
      OrderedMap<String, ?> component = (OrderedMap<String, ?>) components
          .get(i);
      readComponent(component);
    }

  }

  private void readComponent(OrderedMap<String, ?> component) {
    String name = (String) component.get("name");
    Actor actor = originalStage.findActor(name);
    if (actor == null)
      return;

    actor.x = (Float) component.get("x");
    actor.y = (Float) component.get("y");
    actor.originX = (Float) component.get("originX");
    actor.originY = (Float) component.get("originY");
    actor.width = (Float) component.get("width");
    actor.height = (Float) component.get("height");
    actor.visible = (Boolean) component.get("visible");
    if (actor instanceof Box2DActor) {
      Box2DActor box2dActor = (Box2DActor) actor;
      box2dActor.setPositionFromScreen();
      box2dActor.getBody().setActive(actor.visible);
    }
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

  public boolean isEnabled() {
    return isEnabled;
  }
}
