package com.mazalearn.scienceengine.billing;


public interface IBilling {
  
  int REQUEST_CODE = 0xFABB;

  public void purchaseCallback(String productId);
  
  public void inventoryCallback(Inventory inventory);

}
