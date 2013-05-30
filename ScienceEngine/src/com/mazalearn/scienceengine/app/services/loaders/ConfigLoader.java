package com.mazalearn.scienceengine.app.services.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.model.IScience2DModel;

public class ConfigLoader {

  /**
   * Load configs from JSON array
   * @param configs
   * @param science2DModel
   */
  public static void loadConfigs(Array<?> configs, IScience2DModel science2DModel) {
    if (configs == null) return;
    Gdx.app.log(ScienceEngine.LOG, "Loading configs");
    
    for (int i = 0; i < configs.size; i++) {
      @SuppressWarnings("unchecked")
      OrderedMap<String, ?> config = (OrderedMap<String, ?>) configs.get(i);
      ConfigLoader.loadConfig(config, science2DModel);
    }
  }

  @SuppressWarnings("unchecked")
  private static void loadConfig(OrderedMap<String, ?> configObj, IScience2DModel science2DModel) {
    String name = (String) configObj.get("name");
    Gdx.app.log(ScienceEngine.LOG, "Loading config: " + name);
    IModelConfig<?> config = science2DModel.getConfig(name);
    if (config == null) {
      Gdx.app.error(ScienceEngine.LOG, "Config not found:" + name);
      return;
    }
    
    config.setPermitted((Boolean) LevelLoader.nvl(configObj.get("permitted"), false));
    if (configObj.get("value") != null) {
      switch (config.getType()) {
      case TOGGLE:
        ((IModelConfig<Boolean>) config).setValue((Boolean) configObj
            .get("value"));
        break;
      case RANGE:
        ((IModelConfig<Float>) config).setValue((Float) configObj
            .get("value"));
        break;
      case LIST:
        ((IModelConfig<String>) config).setValue((String) configObj
            .get("value"));
        break;
      default:
        break;
      }
    }
  }

}
