package com.mazalearn.scienceengine.core.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mazalearn.scienceengine.ScreenComponent;
import com.mazalearn.scienceengine.app.screens.TopicHomeScreen;

public class Snapshotter extends Actor {
  
  private ShapeRenderer shapeRenderer;

  public Snapshotter() {
    setPosition(0, 0);
    this.shapeRenderer = new ShapeRenderer();
    setSize(ScreenComponent.VIEWPORT_WIDTH, ScreenComponent.VIEWPORT_HEIGHT);
  }
  
  public void draw (SpriteBatch batch, float parentAlpha) {
    batch.end();
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    shapeRenderer.begin(ShapeType.Rectangle);
    shapeRenderer.setColor(Color.GREEN);
    float height = (getWidth() * TopicHomeScreen.THUMBNAIL_HEIGHT) / TopicHomeScreen.THUMBNAIL_WIDTH;
    this.setHeight(height);
    shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
    shapeRenderer.end();
    batch.begin();
  }
  
}
