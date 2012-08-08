package com.mazalearn.scienceengine.experiments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.molecule.LJMolecularModel;
import com.mazalearn.scienceengine.molecule.MolecularModel;

/**
 * States of Matter experiment screen.
 */
public class MoleculeBox extends Actor {
  private static final int BOX_HEIGHT = 20;
  private static final int BOX_WIDTH = 20;
  private static final int N = 25; // Number of molecules
  private static final int PIXEL_DIAMETER = 8;
  
  private MolecularModel molecularModel;
  private Texture moleculeTexture;
  private long timeStart;
  private BitmapFont font;
  private Texture background;

  public MoleculeBox() {
    super();

    Pixmap pixmap = new Pixmap(PIXEL_DIAMETER, PIXEL_DIAMETER, Format.RGBA8888);
    pixmap.setColor(Color.DARK_GRAY);
    pixmap.fillCircle(PIXEL_DIAMETER/2, PIXEL_DIAMETER/2, PIXEL_DIAMETER/2);
    moleculeTexture = new Texture(pixmap);
    pixmap.dispose();
    
    // Use light-gray background color
    pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0,  0, 1, 1);
    background = new Texture(pixmap);
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
    batch.draw(background, this.x, this.y, width, height);
    // Draw the molecules
    float scaleX = this.width / BOX_WIDTH;
    float scaleY = this.height / BOX_HEIGHT;
    for (int i = 0; i < N; i++) {
      batch.draw(moleculeTexture,
          this.x + (float) molecularModel.getMolecule(i).x * scaleX, 
          this.y + (float) molecularModel.getMolecule(i).y * scaleY);
    }
    
    //Draw debug information
    drawDebug(batch);
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
    molecularModel.simulateSteps(10);
  }
  
  public void dispose() {
    moleculeTexture.dispose();
    font.dispose();
  }

  @Override
  public Actor hit(float x, float y) {
    return null;
  }    
}
