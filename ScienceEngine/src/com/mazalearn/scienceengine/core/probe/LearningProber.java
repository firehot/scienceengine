package com.mazalearn.scienceengine.core.probe;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.LevelLoader;
import com.mazalearn.scienceengine.core.controller.IModelConfig;
import com.mazalearn.scienceengine.core.controller.IModelConfig.ConfigType;
import com.mazalearn.scienceengine.core.lang.Variable;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.domains.electromagnetism.model.ComponentType;

// outcome = function of parameter
// doubts on how parameter change affects magnitude of outcome
// Generate A, B as two parameter points.
// Is the outcome stronger at A or B?
public class LearningProber extends AbstractScience2DProber {
  
  private List<Hint> hints = Collections.emptyList();
  
  private IScience2DModel science2DModel;
  private Array<?> configs;
    
  public LearningProber(IScience2DModel science2DModel, ProbeManager probeManager) {
    super(probeManager);
    this.science2DModel = science2DModel;
  }
  
  @Override
  public String getTitle() {
    return "Can you make number of revolutions of the motor +5 by configuring the magnetic field only?";
  }
  
  @Override
  public void reinitialize(float x, float y, float width, float height, boolean probeMode) {
    super.reinitialize(x,  y, 0, 0, probeMode);
    probeManager.setSize(width,  50);
    probeManager.setPosition(0, height - 50);
    LevelLoader.readConfigs(configs, science2DModel);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void activate(boolean activate) {
    if (activate) {
//      probeManager.setupProbeConfigs(Collections.<IModelConfig<?>> emptyList(), false);
    } 
    ScienceEngine.setProbeMode(activate);
    this.setVisible(activate);
  }

  @Override
  public Hint getHint() {
    for (Hint hint: hints) {
      if (hint.getExpr() != null) {
        for (Variable v: hint.getVariables()) {
          String name = v.name();
          IModelConfig<?> config = science2DModel.getConfig(name);
          if (config != null) {
            switch (config.getType()) {
            case RANGE: 
              v.setValue(((IModelConfig<Float>) config).getValue()); break;
            case LIST:
            case TEXT:
              v.setValue(((IModelConfig<String>) config).getValue()); break;
            case TOGGLE:
              boolean b = ((IModelConfig<Boolean>) config).getValue();
              v.setValue(b ? 1 : 0);
              break;
            default:
              throw new IllegalStateException("Unexpected config type in expression");
            }
          } else if (name.lastIndexOf(".") != -1) {
            int pos = name.lastIndexOf(".");
            String componentName = name.substring(0, pos);
            String property = name.substring(pos);
            Science2DBody body = science2DModel.findBody(ComponentType.valueOf(componentName));
            if (body != null) {
              if (property.equals("AngularVelocity")) {
                v.setValue(body.getAngularVelocity());
              } else if (property.equals("Angle")) {
                v.setValue(body.getAngle());
              }
            }
          }
        }
        boolean val = hint.getExpr().fvalue() != 0;
        if (val) return hint;
      }
    }
    return null;
  }

  public void setProbeConfig(Array<?> configs, List<Hint> hints) {
    this.configs = configs;
    this.hints = hints;
  }
}