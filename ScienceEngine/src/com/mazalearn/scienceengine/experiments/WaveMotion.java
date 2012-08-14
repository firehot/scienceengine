package com.mazalearn.scienceengine.experiments;

import java.lang.reflect.Method;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.mazalearn.scienceengine.ScienceEngine;

/**
 * Wave Motion experiment
 */
public class WaveMotion extends Table {
  
  /**
   * Utility class for floating point sliders using reflection.
   *
   */
  class FloatSlider extends Slider {
    final Object obj;
    String property;
    public FloatSlider(float low, float high, float step, Skin skin, String property, Object obj) {
      super(low, high, step, skin);
      this.obj = obj;
      this.property = property;
      setValue(getVal());
      setValueChangedListener(new ValueChangedListener() {
        @Override
        public void changed(Slider slider, float value) {
          setVal(value);
        }      
      });
    }
    float getVal() {
      try {
        return (Float) obj.getClass().getMethod("get" + property).invoke(obj);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return 0;
   }
    void setVal(float value) {
      try {
        Method setter = obj.getClass().getMethod(
            "set" + property, new Class[] {float.class});
        setter.invoke(obj, value);
      } catch (Exception e) {
        e.printStackTrace();
      }     
   }
  }
  public WaveMotion(Skin skin) {
    super(skin);
    if (ScienceEngine.DEV_MODE) {
      debug();
    }
    final WaveString waveString = new WaveString(400, 200);
    this.add(waveString);
    Table controls = new Table(skin);
    this.add(controls).width(20).fill();
    controls.add(new FloatSlider(0, 10, 1, skin, "Tension", waveString));
    controls.row();
    controls.add(new FloatSlider(0, 0.5f, 1, skin, "Damping", waveString));
    controls.row();
    controls.add(new FloatSlider(5, 20, 1, skin, "PulseWidth", waveString));
    controls.row();
    controls.add(new FloatSlider(0, 1, 1, skin, "Frequency", waveString));
    controls.row();
  }

}
