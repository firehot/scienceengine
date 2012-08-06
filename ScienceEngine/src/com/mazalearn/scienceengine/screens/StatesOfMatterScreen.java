package com.mazalearn.scienceengine.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.Delay;
import com.badlogic.gdx.scenes.scene2d.actions.FadeIn;
import com.badlogic.gdx.scenes.scene2d.actions.FadeOut;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.AnimationAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.molecule.LJMolecularModel;
import com.mazalearn.scienceengine.molecule.MolecularModel;
import com.mazalearn.scienceengine.services.MusicManager.ScienceEngineMusic;

/**
 * Shows a splash image and moves on to the next screen.
 */
public class StatesOfMatterScreen extends AbstractScreen {
  SpriteBatch batch;
  OrthographicCamera camera;
  MolecularModel molecularModel;
  static final int N = 25; // Number of molecules
  static final int pixelDiameter = 8;
  Texture moleculeTexture;
  long timeStart;

  public StatesOfMatterScreen(ScienceEngine game) {
    super(game);
    // create the camera and the SpriteBatch
    camera = new OrthographicCamera();
    camera.setToOrtho(false, 800, 480);
    batch = new SpriteBatch();

    Pixmap pixmap = new Pixmap(8, 8, Format.RGBA8888);
    pixmap.setColor(0, 0, 1, 0.75f);
    pixmap.fillCircle(4, 4, 4);
    moleculeTexture = new Texture(pixmap);
    pixmap.dispose();

    // Initialize molecules
    molecularModel = new LJMolecularModel(20, 20, N, 0.5);
    molecularModel.initialize();
    timeStart = System.currentTimeMillis();
  }

  @Override
  public void show() {
    super.show();
  }
  
  @Override
  public void render(float delta) {
    // clear the screen with a dark blue color. The
    // arguments to glClearColor are the red, green
    // blue and alpha component in the range [0,1]
    // of the color to be used to clear the screen.
    Gdx.gl.glClearColor(0.8f, 0.8f, 0.8f, 1);
    Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

    // tell the camera to update its matrices.
    camera.update();

    // tell the SpriteBatch to render in the
    // coordinate system specified by the camera.
    batch.setProjectionMatrix(camera.combined);

    // begin a new batch and draw the molecules
    BitmapFont font = new BitmapFont();
    batch.begin();
    for (int i = 0; i < N; i++) {
      batch.draw(moleculeTexture, (float) molecularModel.getMolecule(i).x
          * pixelDiameter, (float) molecularModel.getMolecule(i).y
          * pixelDiameter);
    }
    font.setColor(0.0f, 0.0f, 0.0f, 1.0f);
    font.draw(batch, String.valueOf(molecularModel.getTemperature()), 10, 20);
    font.draw(batch, String.valueOf(molecularModel.getSimulatedTime()), 10, 300);
    long timeNow = System.currentTimeMillis();
    font.draw(batch, String.valueOf(timeNow - timeStart), 200, 300);
    batch.end();
    font.dispose();
    molecularModel.simulateSteps(10);
  }
  
  @Override
  public void dispose() {
    super.dispose();
    // dispose of all the native resources
    batch.dispose();
    moleculeTexture.dispose();
  }    
}
