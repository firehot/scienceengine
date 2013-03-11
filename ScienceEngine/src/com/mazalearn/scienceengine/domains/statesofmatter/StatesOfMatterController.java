package com.mazalearn.scienceengine.domains.statesofmatter;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mazalearn.scienceengine.Topic;
import com.mazalearn.scienceengine.core.controller.AbstractScience2DController;
import com.mazalearn.scienceengine.core.model.IScience2DModel;
import com.mazalearn.scienceengine.core.model.Science2DBody;
import com.mazalearn.scienceengine.core.view.ColorPanel;
import com.mazalearn.scienceengine.core.view.ModelControls;
import com.mazalearn.scienceengine.domains.statesofmatter.model.ComponentType;
import com.mazalearn.scienceengine.domains.statesofmatter.view.MoleculeBoxActor;

/**
 * States of Matter science2DModel
 */
public class StatesOfMatterController extends AbstractScience2DController {
  private static final int N = 25; // Number of molecules

  private IScience2DModel statesOfMatterModel;
  private StatesOfMatterView statesOfMatterView;
  ModelControls modelControls;
  
  public StatesOfMatterController(Topic level, int width, int height, Skin skin) {
    super(Topic.StatesOfMatter, level, skin);
    statesOfMatterModel = new StatesOfMatterModel(N);
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
        ScienceEngine.getTextureRegion("iceberg")),
        ScienceEngine.getTextureRegion("fire-texture")),            
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
    ComponentType componentType;
    try {
      componentType = ComponentType.valueOf(type);
    } catch(IllegalArgumentException e) {
      return super.createActor(type, viewSpec, body);
    }
    switch (componentType) {
    case MoleculeBox:
      return new MoleculeBoxActor(body, N, boxWidth, boxHeight, science2DView.getFont());
    case ColorPanel:
      return new ColorPanel(type);
    }
    return null;
  }
}
