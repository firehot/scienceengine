package com.mazalearn.scienceengine.app.services;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.utils.Array;
import com.mazalearn.scienceengine.core.controller.IScience2DController;

/** {@link AssetLoader} for Level instances. The Level is loaded asynchronously.
 * @author sridhar sundaram */
public class AsyncLevelLoader extends AsynchronousAssetLoader<IScience2DController, AsyncLevelLoader.LevelLoaderParameter> {
  public AsyncLevelLoader (FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync (AssetManager manager, String fileName, LevelLoaderParameter parameter) {
  }

  @Override
  public IScience2DController loadSync (AssetManager manager, String fileName, LevelLoaderParameter parameter) {
    new LevelLoader(parameter.science2DController).load();
    return parameter.science2DController;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Array<AssetDescriptor> getDependencies (String fileName, LevelLoaderParameter parameter) {
    return null;
  }

  static public class LevelLoaderParameter extends AssetLoaderParameters<IScience2DController> {
    public IScience2DController science2DController;
  }
}
