package com.mazalearn.scienceengine.experiments.molecules.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.experiments.molecules.model.IMolecularModel;

final class MoleculeBox extends Actor {
  /**
   * 
   */
  private IMolecularModel molecularModel;
  private Texture moleculeTextureGray, moleculeTextureRed;
  private long timeStart;
  private BitmapFont font;
  private Texture backgroundTexture;
  private final int boxHeight;
  private final int boxWidth;
  private final int N;

  public MoleculeBox(IMolecularModel molecularModel, int N, int boxWidth, int boxHeight, int pixelsPerM) {
    this.molecularModel = molecularModel;
    this.boxWidth = boxWidth;
    this.boxHeight = boxHeight;
    this.N = N;
    
    Pixmap pixmap = new Pixmap(pixelsPerM, pixelsPerM, Format.RGBA8888);
    pixmap.setColor(Color.DARK_GRAY);
    pixmap.fillCircle(pixelsPerM / 2, pixelsPerM / 2, pixelsPerM / 2);
    moleculeTextureGray = new Texture(pixmap);
    pixmap.setColor(Color.RED);
    pixmap.fillCircle(pixelsPerM / 2, pixelsPerM / 2, pixelsPerM / 2);
    moleculeTextureRed = new Texture(pixmap);
    pixmap.dispose();
    
    // Use light-gray background color
    pixmap = new Pixmap(1, 1, Format.RGBA8888);
    pixmap.setColor(Color.LIGHT_GRAY);
    pixmap.fillRectangle(0, 0, 1, 1);
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    
    font = new BitmapFont();
    timeStart = System.currentTimeMillis();
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    // Draw background
    batch.draw(backgroundTexture, this.x, this.y, this.width, this.height);
    // Draw the molecules
    float scaleX = this.width / boxWidth;
    float scaleY = this.height / boxHeight;
    for (int i = 0; i < N; i++) {
      batch.draw(i > 0 ? moleculeTextureGray : moleculeTextureRed,
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
  }
  
  @Override
  public Actor hit(float x, float y) {
    return null;
  }

  public void dispose() { // TODO: call this
    moleculeTextureGray.dispose();
    backgroundTexture.dispose();
    font.dispose();
  }
}