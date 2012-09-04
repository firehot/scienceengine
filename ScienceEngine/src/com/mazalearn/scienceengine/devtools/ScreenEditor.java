package com.mazalearn.scienceengine.devtools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.box2d.Box2DActor;

/**
 * A ScreenEditor lets you edit your objects directly in the game. No more
 * hardcoded values for positions and sizes, no more headaches trying to
 * precisely put an object at its place, or trying to align UI components.
 * <br/><br/>
 *
 * The editor, once enabled, will replace your InputProcessor by its own until
 * you deactivate it. Thanks to this mechanism, you will be able to zoom and pan
 * your scene to easily adjust the components.
 * <br/><br/>
 *
 * Usage is as follows:<br/>
 * 1. Create an actors map and add your actors to it.<br/>
 * 2. Create the ScreenEditor with the list, the screen camera, and a path to
 * the file where things will get saved.<br/>
 * 3. Call load() method to import previously saved stuff, if any.<br/>
 * 4. Call enable() method if you want to enable the editor to make changes to
 * your scene.<br/>
 * 5. Call render() in your rendering loop to show the overlay.<br/><br/>
 *
 * <b>Note that the everything you need to remove once you don't want to make
 * anymore changes is the call to enable(). That's all. Yes. Happy?</b>
 *
 * @author Aurelien Ribon (aurelien.ribon@gmail.com)
 */
public class ScreenEditor {
  private static final short VERSION = 0;
  
  private final FileHandle file;
  private final OrthographicCamera camera;
  private final Map<String, Actor> actors;

  private boolean isEnabled = false;
  enum OverlayMode { NO_OVERLAY, OVERLAY_NO_HELP, OVERLAY_WITH_HELP};
  private OverlayMode overlayMode = OverlayMode.OVERLAY_WITH_HELP;
  private ImmediateModeRenderer20 imr;
  private SpriteBatch sb;
  private BitmapFont font;
  private Color fontColor = Color.WHITE;
  private Actor selectedActor;

  private Vector3 origCameraPos;
  private float origCameraZoom;
  private InputProcessor origInputProcessor;

  /**
   * Build and initialize the editor.
   * @param path The path of the file that will be loaded/written.
   * @param camera The camera used to set up the screen.
   * @param actors A map from names to actors.
   */
  public ScreenEditor(String path, OrthographicCamera camera, Map<String, Actor> actors, SpriteBatch sb, BitmapFont font) {
    this.file = Gdx.files.internal(path);
    this.camera = camera;
    this.actors = actors;
    this.sb = sb;
    this.font = font;
  }

  /**
   * Loads the content of the provided file and automatically position and
   * size the objects.
   */
  public void load() {
    try {
      loadFile();
      System.out.println("[ScreenEditor] File successfully loaded!");
    } catch (GdxRuntimeException ex) {
      System.err.println("[ScreenEditor] Error happened while loading " + file.path());
    }
    
  }

  /**
   * Enables the editor. Creates all required resources and replace the
   * current InputProcessor by its own. Just remove this call from your code
   * once you're happy with the result. Any other call can stay without any
   * side-effect.
   */
  public void enable() {
    if (!isEnabled) {
      imr = new ImmediateModeRenderer20(64, false, true, 0);
    }
    origCameraZoom = camera.zoom;
    origCameraPos = camera.position.cpy();
    origInputProcessor = Gdx.input.getInputProcessor();
    Gdx.input.setInputProcessor(inputProcessor);
    isEnabled = true;
  }

  /**
   * Renders the editor overlay.
   */
  public void render() {
    if (isEnabled && overlayMode != OverlayMode.NO_OVERLAY) {
      for (Actor actor : actors.values()) {
        drawBoundingBox(actor);
      }

      int top = Gdx.graphics.getHeight() + 5;
      switch (overlayMode) {
        case OVERLAY_WITH_HELP:
          sb.begin();
          font.setColor(fontColor);
          font.draw(sb, "Screen EditorScreen - rev " + VERSION, 5, top - 15*0);
          font.draw(sb, "---------------------------------------------------------------", 5, top - 15*1);
          font.draw(sb, "'s' to save, 'l' to reload", 5, top - 15*2);
          font.draw(sb, "'space' to toggle overlay, 'enter' to exit", 5, top - 15*3);
          font.draw(sb, "scroll to zoom, hold right clic to pan, 'r' to reset camera", 5, top - 15*4);
          font.draw(sb, "---------------------------------------------------------------", 5, top - 15*5);
          font.draw(sb, selectedActor != null ? getInfo(selectedActor) : "> No object selected", 5, top - 15*6);
          sb.end();
          break;

        case OVERLAY_NO_HELP:
          sb.begin();
          font.setColor(fontColor);
          font.draw(sb, "Screen EditorScreen - rev " + VERSION, 5, top - 15*0);
          font.draw(sb, "---------------------------------------------------------------", 5, top - 15*1);
          font.draw(sb, selectedActor != null ? getInfo(selectedActor) : "> No object selected", 5, top - 15*2);
          sb.end();
          break;
      }
    }
  }

  // -------------------------------------------------------------------------
  // Inputs
  // -------------------------------------------------------------------------

  private final InputProcessor inputProcessor = new InputAdapter() {
    private final Vector2 lastTouch = new Vector2();
    private Actor draggedActor = null;
    private Actor resizedActor = null;

    @Override public boolean touchDown(int x, int y, int pointer, int button) {
      Vector2 p = screenToWorld(x, y);

      switch (button) {
        case Buttons.LEFT:
          for (Actor actor : actors.values()) {
            if (actor.x <= p.x && p.x <= actor.x + actor.width &&
              actor.y <= p.y && p.y <= actor.y + actor.height) {

              Vector2 handleSize = screenToWorld(10, -10).sub(screenToWorld(0, 0));
              Vector2 handlePos = 
                  new Vector2(actor.x + actor.width - handleSize.x, 
                      actor.y + actor.height - handleSize.y);

              if (handlePos.x <= p.x && p.x <= handlePos.x + handleSize.x &&
                handlePos.y <= p.y && p.y <= handlePos.y + handleSize.y) {
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

    @Override public boolean touchUp(int x, int y, int pointer, int button) {
      switch (button) {
        case Buttons.LEFT:
          draggedActor = resizedActor = selectedActor = null;
          break;
      }

      lastTouch.set(x, y);
      return true;
    }

    @Override public boolean touchDragged(int x, int y, int pointer) {
      Vector2 delta = new Vector2(x, y).sub(lastTouch).mul(camera.zoom);
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

    @Override public boolean scrolled(int amount) {
      camera.zoom *= amount > 0 ? 1.2f : 1/1.2f;
      camera.update();
      return true;
    }

    @Override public boolean keyDown(int keycode) {
      switch (keycode) {
        case Keys.SPACE:
          switch(overlayMode) {
            case NO_OVERLAY: overlayMode = OverlayMode.OVERLAY_NO_HELP; break;
            case OVERLAY_NO_HELP: overlayMode = OverlayMode.OVERLAY_WITH_HELP; break;
            case OVERLAY_WITH_HELP: overlayMode = OverlayMode.NO_OVERLAY; break;
          }
          break;
        case Keys.S: save(); break;
        case Keys.L: load(); break;
        case Keys.R: resetCamera(); break;
        case Keys.ENTER: disable(); break;
      }
      return true;
    }
  };

  // -------------------------------------------------------------------------
  // Internals
  // -------------------------------------------------------------------------

  private void save() {
    try {
      writeFile();
      System.out.println("[ScreenEditor] File successfully saved!");
    } catch (IOException ex) {
      System.err.println("[ScreenEditor] Error happened while writing " + file.path());
    }
  }

  private void disable() {
    if (isEnabled) {
      imr = null;
      resetCamera();
      Gdx.input.setInputProcessor(origInputProcessor);
      isEnabled = false;
    }
  }

  private String getInfo(Actor actor) {
    return String.format(Locale.US, "> %s > xy:[%.3f,%.3f] wh:[%.3f,%.3f]",
      getName(actors, actor), actor.x, actor.y, actor.width, actor.height);
  }

  private String getName(Map<String, Actor> actors, Actor actor) {
    for (Entry<String, Actor> entry: actors.entrySet()) {
      if (entry.getValue() == actor) {
        return entry.getKey();
      }
    }
    return null;
  }

  private void resetCamera() {
    camera.zoom = origCameraZoom;
    camera.position.set(origCameraPos);
    camera.update();
  }

  private void writeFile() throws IOException {
    FileWriter writer = new FileWriter(file.file());
    JsonWriter jsonWriter = new JsonWriter(writer);
    jsonWriter = jsonWriter.object().array("components");
    for (Entry<String, Actor> entry : actors.entrySet()) {
      Actor a = entry.getValue();
      jsonWriter.object()
          .object(entry.getKey())
          .set("x", a.x)
          .set("y", a.y)
          .set("originX", a.originX)
          .set("originY", a.originY)
          .set("width", a.width)
          .set("height", a.height) 
          .pop()
          .pop();
    }
    jsonWriter.flush();
    jsonWriter.close();
  }
  
  @SuppressWarnings("unchecked")
  private void loadFile() {
    String str = file.readString();
    OrderedMap<String,?> rootElem = 
        (OrderedMap<String,?>) new JsonReader().parse(str);

    Array<?> components = (Array<?>) rootElem.get("components");
    for (int i = 0; i < components.size; i++) {
      OrderedMap<String,?> component = (OrderedMap<String,?>) components.get(i);
      readComponent(component);
    }

  }

  private void readComponent(OrderedMap<String,?> component) {
    String name = (String) component.get("name");
    Actor actor = this.actors.get(name);
    if (actor == null) return;
    
    actor.x = (Float) component.get("x");;
    actor.y = (Float) component.get("y");
    actor.originX = (Float) component.get("originX");
    actor.originY = (Float) component.get("originY");
    actor.width = (Float) component.get("width");
    actor.height = (Float) component.get("height");
    if (actor instanceof Box2DActor) {
      ((Box2DActor) actor).setPositionFromScreen();
    }
  }

  private Vector2 screenToWorld(int x, int y) {
    Vector3 v3 = new Vector3(x, y, 0);
    camera.unproject(v3);
    return new Vector2(v3.x, v3.y);
  }

  private void drawBoundingBox(Actor actor) {
    Vector2 objPos = new Vector2(actor.x + actor.width/2, actor.y + actor.height/2);
    drawRect(objPos, actor.width, actor.height, Color.BLUE, 2);

    Vector2 handleSize = screenToWorld(10, -10).sub(screenToWorld(0, 0));
    Vector2 handlePos = new Vector2(actor.x + actor.width - handleSize.x/2, actor.y + actor.height - handleSize.y/2);
    drawRect(handlePos, handleSize.x, handleSize.y, Color.BLUE, 2);
  }

  private void drawRect(Vector2 p, float w, float h, Color c, float lineWidth) {
    Gdx.gl20.glLineWidth(lineWidth);
    imr.begin(sb.getProjectionMatrix(), GL10.GL_LINE_LOOP);
    imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x - w/2, p.y - h/2, 0);
    imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x - w/2, p.y + h/2, 0);
    imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x + w/2, p.y + h/2, 0);
    imr.color(c.r, c.g, c.b, c.a); imr.vertex(p.x + w/2, p.y - h/2, 0);
    imr.end();
  }

  public boolean isEnabled() {
    return isEnabled;
  }
}
