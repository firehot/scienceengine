  static class ImageList extends List {
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
        Image image = images[i-1];
        image.setClickListener(new ClickListener() {
          @Override
          public void click(Actor actor, float x, float y) {
            levelManager.setLevel(iLevel);
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
      shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
      stage.getSpriteBatch().begin();
      shapeRenderer.begin(ShapeType.Rectangle);
      shapeRenderer.setColor(Color.YELLOW);
      shapeRenderer.rect(image.x, image.y, image.width, image.height);
      shapeRenderer.end();
      stage.getSpriteBatch().end();
    }
  }
