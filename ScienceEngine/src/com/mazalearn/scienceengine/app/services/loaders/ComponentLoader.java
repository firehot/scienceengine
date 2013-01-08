package com.mazalearn.scienceengine.app.services.loaders;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.model.ComponentType;
import com.mazalearn.scienceengine.core.model.EnvironmentBody;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.IScience2DView;
import com.mazalearn.scienceengine.core.view.Science2DActor;

public class ComponentLoader {

  private IScience2DModel science2DModel;
  private IScience2DView science2DView;
  private Map<String, Integer> componentTypeCount = new HashMap<String, Integer>();

  public ComponentLoader(IScience2DModel science2DModel,
      IScience2DView science2DView) {
    this.science2DModel = science2DModel;
    this.science2DView = science2DView;
  }

  private void loadEnvironment(EnvironmentBody environment,
      Array<?> environmentParams) {
    Gdx.app.log(ScienceEngine.LOG, "Loading environment");
    for (int i = 0; i < environmentParams.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> parameter = (OrderedMap<String, ?>) environmentParams
          .get(i);
      String parameterName = (String) parameter.get("name");
      environment.addParameter(parameterName);
    }
  }

  public void loadComponents(Array<?> components, boolean create) {
    if (components == null)
      return;
    if (create) {
      componentTypeCount.clear();
    }
    Gdx.app.log(ScienceEngine.LOG, "Loading components");

    for (int i = 0; i < components.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> component = (OrderedMap<String, ?>) components
          .get(i);
      loadComponent(component, create);
    }
  }

  @SuppressWarnings("unchecked")
  private void loadComponent(OrderedMap<String, ?> component, boolean create) {
    String type = (String) component.get("type");
    Gdx.app.log(ScienceEngine.LOG, "Loading component: " + type);
    if (type == null)
      return;
    float x = (Float) LevelLoader.nvl(component.get("x"), 0f);
    float y = (Float) LevelLoader.nvl(component.get("y"), 0f);
    float rotation = (Float) LevelLoader.nvl(component.get("rotation"), 0f);

    Actor actor = create ? createActor(type, x, y, rotation) : findActor(type);
    if (actor == null) {
      Gdx.app.log(ScienceEngine.LOG, "Ignoring - Could not load component: "
          + type);
      return;
    }
    if (component.get("x") != null)
      actor.setX(x);
    if (component.get("y") != null)
      actor.setY(y);
    if (component.get("originX") != null)
      actor.setOriginX((Float) component.get("originX"));
    if (component.get("originY") != null)
      actor.setOriginY((Float) component.get("originY"));
    if (component.get("width") != null)
      actor.setWidth((Float) component.get("width"));
    if (component.get("height") != null)
      actor.setHeight((Float) component.get("height"));
    if (component.get("visible") != null)
      actor.setVisible((Boolean) component.get("visible"));
    if (component.get("rotation") != null)
      actor.setRotation(rotation);
    if (actor instanceof Science2DActor) {
      Science2DActor science2DActor = (Science2DActor) actor;
      science2DActor.setPositionFromViewCoords(false);
      if (component.get("move") != null) {
        science2DActor.setMovementMode((String) component.get("move"));
      }
      if (component.get("extra") != null)
        science2DActor.getBody().setExtra((String) component.get("extra"));
      if ((Boolean) LevelLoader.nvl(component.get("bodytype"), false)) {
        science2DActor.getBody().setType(BodyType.DynamicBody);
      } else {
        science2DActor.getBody().setType(BodyType.StaticBody);
      }
      if (ComponentType.Environment.name().equals(type)) {
        loadEnvironment((EnvironmentBody) science2DActor.getBody(),
            (Array<String>) component.get("params"));
      }
    }
  }

  private Actor createActor(String type, float x, float y, float rotation) {
    Science2DBody science2DBody = 
        science2DModel.addBody(type, x / ScienceEngine.PIXELS_PER_M, 
            y / ScienceEngine.PIXELS_PER_M, 
            rotation * MathUtils.degreesToRadians);
    Actor actor = (science2DBody != null) 
        ? science2DView.addScience2DActor(science2DBody)
        : science2DView.addVisualActor(type);
    if (actor == null && type.equals("ControlPanel")) {
      actor = science2DView.findActor(type);
    }
    return actor;
  }

  private Actor findActor(String type) {
    Actor actor = science2DView.findActor(type);
    // If multiple actors of same type, they have number suffix 1,2,3...
    if (actor == null) {
      Integer count = (Integer) LevelLoader
          .nvl(componentTypeCount.get(type), 0) + 1;
      componentTypeCount.put(type, count);
      actor = science2DView.findActor(type + "." + count);
    }
    return actor;
  }
}