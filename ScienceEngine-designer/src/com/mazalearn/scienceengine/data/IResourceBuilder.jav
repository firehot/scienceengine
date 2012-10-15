package com.mazalearn.scienceengine.app.services.data;

/**
 * Defines an API for Resource builders.
 * 
 * @author acoppes
 * 
 */
public interface IResourceBuilder<T> {

  /**
   * Returns true if the Resource should be considered volatile and not cached, false otherwise.
   */
  boolean isVolatile();

  /**
   * Defines how to build the data to be used in the Resource.
   */
  T build();

  // could receive the resource manager as a parameter to the build if the resource builder need it to get other resources
  // T build(ResourceManager resourceManager) 

}