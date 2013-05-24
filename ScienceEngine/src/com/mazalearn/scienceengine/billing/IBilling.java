package com.mazalearn.scienceengine.billing;

import com.mazalearn.scienceengine.Topic;

public interface IBilling {
  
  int REQUEST_CODE = 0xFABB;

  public void purchaseCallback(Topic topic);

}
