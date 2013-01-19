package com.mazalearn.scienceengine.app.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * List class for choosing an image from a list
 */
public class ImageList {
  
};
/*
public class ImageList extends List {
  ShapeRenderer shapeRenderer;
  Table table;

  public ImageList(String name, Image[] images, Skin skin, int numColumns) {
    super(images, skin);
    shapeRenderer = new ShapeRenderer();
    Table table = new Table(skin, null, name);
    table.add(name).colspan(numColumns);
    table.row();
    for (int i = 1; i <= numColumns; i++) {
      final int iLevel = i;
      Image image = images[i - 1];
      image.addListener(new ClickListener() {
        @Override
        public void click(Actor actor, float x, float y) {
        }
      });
      table.add(image).pad(5);
      if (i % 3 == 0) {
        table.row();
      }
    }
    table.row();
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    table.draw(batch, parentAlpha);
    Image image = null;
    shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
    batch.begin();
    shapeRenderer.begin(ShapeType.Rectangle);
    shapeRenderer.setColor(Color.YELLOW);
    shapeRenderer.rect(image.getX(), image.getY(), image.getWidth(), image.getHeight());
    shapeRenderer.end();
    batch.end();
  }
}
*/