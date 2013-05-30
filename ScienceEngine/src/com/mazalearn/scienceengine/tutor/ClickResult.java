package com.mazalearn.scienceengine.tutor;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mazalearn.scienceengine.ScienceEngine;
import com.mazalearn.scienceengine.app.services.SoundManager.ScienceEngineSound;

/**
 * Listener Class which allows one of multiple image results based on 
 * touchdrag position with respect to an image to be selected.
 * @author sridhar
 *
 */
class ClickResult extends ClickListener {
  private final IDoneCallback doneCallback;
  private int resultImage;
  private Image[] stateImages;
  private StateMapper stateMapper;
  
  interface StateMapper {
    public int map(float x, float y);
  }

  ClickResult(IDoneCallback doneCallback, Image[] stateImages, 
      StateMapper stateMapper) {
    this.doneCallback = doneCallback;
    this.stateImages = stateImages;
    this.stateMapper = stateMapper;
  }
  
  public void setResult(int resultImage) {
    this.resultImage = resultImage;
  }

  @Override
  public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
    super.touchDown(event, x, y, pointer, button);
    stateImages[0].setVisible(true);
    return true;
  }
  
  @Override
  public void touchDragged(InputEvent event, float x, float y, int pointer) {
    for (Image image: stateImages) {
      image.setVisible(false);
    }
    stateImages[stateMapper.map(x, y)].setVisible(true);
  }
  
  @Override
  public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
    super.touchUp(event, x, y, pointer, button);
    ScienceEngine.getSoundManager().play(ScienceEngineSound.CLICK);
    boolean success = stateImages[resultImage].isVisible();
    for (Image image: stateImages) {
      image.setVisible(false);
    }
    doneCallback.done(success);
  }
};

