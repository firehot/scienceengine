package com.mazalearn.scienceengine.guru;

import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.mazalearn.scienceengine.core.controller.IModelConfig;

public class ConfigGenerator {
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void generateConfig(List<IModelConfig<?>> modelConfigs) {
    for (IModelConfig<?> config: modelConfigs) {
      if (config.hasProbeMode()) {
        config.setProbeMode();
        continue;
      }
      switch(config.getType()) {
      case LIST:
        IModelConfig<String> listConfig = (IModelConfig<String>) config;
        Enum[] list = listConfig.getList();
        int index = MathUtils.random(0, list.length - 1);
        listConfig.setValue(list[index].name());
        break;
      case RANGE:
        IModelConfig<Float> rangeConfig = (IModelConfig<Float>) config;
        float low = rangeConfig.getLow();
        float high = rangeConfig.getHigh();
        float delta = (high - low) * 0.1f;
        float value = MathUtils.random(low + delta, high - delta);
        rangeConfig.setValue(value);
        break;
      case TOGGLE:
        IModelConfig<Boolean> onOffConfig = (IModelConfig<Boolean>) config;
        boolean on = MathUtils.random(0, 1) == 1;
        onOffConfig.setValue(on);
        break;
      case COMMAND:
        IModelConfig<String> commandConfig = (IModelConfig<String>) config;
        boolean doCommand = MathUtils.randomBoolean();
        if (doCommand) commandConfig.doCommand();
        break;
      default:
        throw new IllegalArgumentException("Unknown config type: " + config);
      }
    }
  }
}
