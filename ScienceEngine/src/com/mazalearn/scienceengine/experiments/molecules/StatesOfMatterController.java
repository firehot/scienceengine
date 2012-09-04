package com.mazalearn.scienceengine.experiments.molecules;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.controller.AbstractExperimentController;
import com.mazalearn.scienceengine.controller.Configurator;
import com.mazalearn.scienceengine.experiments.molecules.model.IMolecularModel;
import com.mazalearn.scienceengine.experiments.molecules.model.LJMolecularModel;
import com.mazalearn.scienceengine.experiments.molecules.view.StatesOfMatterView;

/**
 * States of Matter experimentModel
 */
public class StatesOfMatterController extends AbstractExperimentController {
  private static final int N = 25; // Number of molecules
  private static final int BOX_HEIGHT = 20;
  private static final int BOX_WIDTH = 20;

  private IMolecularModel statesOfMatterModel;
  private StatesOfMatterView statesOfMatterView;
  Configurator configurator;
  
  public StatesOfMatterController(int width, int height, Skin skin) {
    super(skin);
    statesOfMatterModel = new LJMolecularModel(BOX_WIDTH, BOX_HEIGHT, N, 0.5);
    statesOfMatterModel.reset();
    statesOfMatterView = new StatesOfMatterView(statesOfMatterModel, BOX_WIDTH, BOX_HEIGHT, N);
    initialize(statesOfMatterModel, statesOfMatterView);    
    
    /*
    // Floor of box - also controls heating.
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0, 0, 1, 1);

    final TextureRegion[] textureRegion = {
        new TextureRegion(new Texture(pixmap)),
        new TextureRegion(new Texture("images/iceberg.jpg")),
        new TextureRegion(new Texture("images/fire-texture.jpg")),            
    };
    pixmap.dispose();
    final Image heatingControl = new Image(textureRegion[0], Scaling.stretch);
    //new TextButton("", skin);
    heatingControl.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        temperatureLevel = (temperatureLevel + 1) % 3;
        Heating tLevel = Heating.NEUTRAL;
        switch (temperatureLevel) {
        case 0: tLevel = Heating.NEUTRAL; break;
        case 1: tLevel = Heating.COLD; break;
        case 2: tLevel = Heating.HOT; break;
        }
        statesOfMatterView.setHeating(tLevel);
        heatingControl.setRegion(textureRegion[temperatureLevel]);
      }
    });
    add(heatingControl).fill().colspan(3).height(30);
    row(); 
  }

  private TextButton createStateButton(Skin skin, LIST caption, 
      final double temperature) {
    TextButton textButton = new TextButton(caption, skin);
    textButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        statesOfMatterModel.setTemperature(temperature);
      }
    });
    return textButton;*/
  }
}
