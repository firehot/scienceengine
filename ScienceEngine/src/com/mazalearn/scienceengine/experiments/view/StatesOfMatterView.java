package com.mazalearn.scienceengine.experiments.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.experiments.model.molecule.LJMolecularModel;
import com.mazalearn.scienceengine.experiments.model.molecule.MolecularModel;
import com.mazalearn.scienceengine.experiments.model.molecule.MolecularModel.Heating;

/**
 * States of Matter experiment View.
 */
public class StatesOfMatterView extends Actor {
  private static final int BOX_HEIGHT = 20;
  private static final int BOX_WIDTH = 20;
  private static final int N = 25; // Number of molecules
  private static final int PIXEL_DIAMETER = 8;
  
  private MolecularModel molecularModel;
  private Texture moleculeTextureGray, moleculeTextureRed;
  private long timeStart;
  private BitmapFont font;
  private Texture backgroundTexture;

  public StatesOfMatterView() {
    super();

    Pixmap pixmap = new Pixmap(PIXEL_DIAMETER, PIXEL_DIAMETER, Format.RGBA8888);
    pixmap.setColor(Color.DARK_GRAY);
    pixmap.fillCircle(PIXEL_DIAMETER/2, PIXEL_DIAMETER/2, PIXEL_DIAMETER/2);
    moleculeTextureGray = new Texture(pixmap);
    pixmap.setColor(Color.RED);
    pixmap.fillCircle(PIXEL_DIAMETER/2, PIXEL_DIAMETER/2, PIXEL_DIAMETER/2);
    moleculeTextureRed = new Texture(pixmap);
    pixmap.dispose();
    
    // Use light-gray background color
    pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0, 0, 1, 1);
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    
    // Initialize molecules
    molecularModel = new LJMolecularModel(BOX_WIDTH, BOX_HEIGHT, N, 0.5);
    molecularModel.initialize();
    font = new BitmapFont();
    timeStart = System.currentTimeMillis();
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw background
    batch.draw(backgroundTexture, this.x, this.y, this.width, this.height);
    // Draw the molecules
    float scaleX = this.width / BOX_WIDTH;
    float scaleY = this.height / BOX_HEIGHT;
    for (int i = 0; i < N; i++) {
      batch.draw(i > 0 ? moleculeTextureGray : moleculeTextureRed,
          this.x + (float) molecularModel.getMolecule(i).x * scaleX, 
          this.y + (float) molecularModel.getMolecule(i).y * scaleY);
    }
    
    //Draw debug information
    drawDebug(batch);
    molecularModel.simulateSteps(10);
  }
  
  public void drawDebug(SpriteBatch batch) {
    // Draw debug information
    font.setColor(0.0f, 0.0f, 0.0f, 1.0f);
    font.draw(batch, String.valueOf(molecularModel.getTemperature()), 
        this.x + 10, this.y + 20);
    font.draw(batch, String.valueOf(molecularModel.getSimulatedTime()), 
        this.x + 10, this.y + 300);
    long timeNow = System.currentTimeMillis();
    font.draw(batch, String.valueOf(timeNow - timeStart), 
        this.x + 200, this.y + 300);
  }
  
  public void dispose() {
    moleculeTextureGray.dispose();
    font.dispose();
  }

  @Override
  public Actor hit(float x, float y) {
    return null;
  }

  public void setHeating(Heating heating) {
    molecularModel.setHeatingLevel(heating);   
  }    

  public void setTemperature(double temperature) {
    molecularModel.setTemperature(temperature);   
  }    
}
