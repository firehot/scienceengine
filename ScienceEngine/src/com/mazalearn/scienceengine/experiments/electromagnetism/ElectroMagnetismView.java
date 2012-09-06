package com.mazalearn.scienceengine.experiments.electromagnetism;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.box2d.Box2DActor;
import com.mazalearn.scienceengine.box2d.ScienceBody;
import com.mazalearn.scienceengine.experiments.electromagnetism.model.Lightbulb;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.BarMagnetView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.CompassView;
import com.mazalearn.scienceengine.experiments.electromagnetism.view.LightbulbView;
import com.mazalearn.scienceengine.view.AbstractExperimentView;

public class ElectroMagnetismView extends AbstractExperimentView {
  public ElectroMagnetismView(float width, float height, final ElectroMagnetismModel emModel) {
    super(emModel, width, height);
    this.width = width;
    this.height = height;
    
    // TODO: use blending function to draw coilsback?
    //Actor coilsBack = new Image(new Texture("images/coppercoils-back.png"),
    //    Scaling.stretch, Align.CENTER, "CoilsBack");
    //this.addActor(coilsBack);
    for (final ScienceBody body: emModel.getBodies()) {
      TextureRegion textureRegion = getTextureRegionForBody(body.getName());
      if (body.getName() == "BarMagnet") {       
        this.addActor(new BarMagnetView(textureRegion, body, this, emModel));
      } else if (body.getName() == "Lightbulb") {
        this.addActor(new LightbulbView(textureRegion, (Lightbulb) body));
      } else if (body.getName() == "PickupCoil") {
        //coilsBack.x = body.getPosition().x * PIXELS_PER_M;
        //coilsBack.y = body.getPosition().y * PIXELS_PER_M;
        this.addActor(new Box2DActor(body, textureRegion));
      } else if (body.getName() == "Compass") {
        this.addActor(new CompassView(textureRegion, body));
      } else {
        this.addActor(new Box2DActor(body, textureRegion));
      }
    }
  }
  
  private TextureRegion getTextureRegionForBody(String name) {
    Texture texture;
    if (name == "BarMagnet") {
      texture = new Texture("images/barmagnet-pivoted.png");
    } else if (name == "PickupCoil") {
      texture = new Texture("images/coppercoils-front.png");
    } else if (name == "Lightbulb") {
      texture = new Texture("images/lightbulb.png");
    } else if (name == "Compass") {
      texture = new Texture("images/compass.png");
    } else {
      return null;
    }
    return new TextureRegion(texture);
  }
}
