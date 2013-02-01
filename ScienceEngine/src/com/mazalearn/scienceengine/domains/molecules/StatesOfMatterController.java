package com.mazalearn.scienceengine.domains.molecules;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.core.controller.AbstractScience2DController;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.ColorPanel;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.domains.molecules.model.IMolecularModel;
import com.mazalearn.scienceengine.domains.molecules.model.LJMolecularModel;
import com.mazalearn.scienceengine.domains.molecules.view.MoleculeBox;
import com.mazalearn.scienceengine.domains.molecules.view.StatesOfMatterView;

/**
 * States of Matter science2DModel
 */
public class StatesOfMatterController extends AbstractScience2DController {
  public static final String DOMAIN = "StatesOfMatter";
  private static final int N = 25; // Number of molecules
  private static final int BOX_HEIGHT = 20;
  private static final int BOX_WIDTH = 20;

  private IMolecularModel statesOfMatterModel;
  private StatesOfMatterView statesOfMatterView;
  ModelControls modelControls;
  
  public StatesOfMatterController(int level, int width, int height, Skin skin) {
    super(DOMAIN, level, skin);
    statesOfMatterModel = new LJMolecularModel(BOX_WIDTH, BOX_HEIGHT, N, 0.5);
    statesOfMatterModel.reset();
    statesOfMatterView = 
        new StatesOfMatterView(statesOfMatterModel, width, height, N, skin, this);
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
    TextButton toggleButton = new TextButton(caption, skin);
    toggleButton.setClickListener(new ClickListener() {
      @Override
      public void click(Actor actor, float x, float y) {
        statesOfMatterModel.setTemperature(temperature);
      }
    });
    return toggleButton;*/
  }

  @Override
  protected Actor createActor(String type, String viewSpec, Science2DBody body) {
    int boxWidth = 20;
    int boxHeight = 20;
    if (type.equals("MoleculeBox")) {
      return new MoleculeBox(statesOfMatterModel, N, boxWidth, boxHeight, 
          science2DView.getFont());
    } else if (type.equals("ColorPanel")) {
      return new ColorPanel(type);
    }
    return null;
  }
}
