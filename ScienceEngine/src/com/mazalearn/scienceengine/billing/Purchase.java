/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mazalearn.scienceengine.billing;

import com.badlogic.gdx.utils.Json;

/**
 * Represents an in-app billing purchase.
 */
public class Purchase {
    String itemType;  // ITEM_TYPE_INAPP or ITEM_TYPE_SUBS
    String orderId;
    String packageName;
    String sku;
    long purchaseTime;
    int purchaseState;
    String developerPayload;
    String token;
    String originalJson;
    String signature;

    public static Purchase toPurchase(String itemType, String jsonPurchaseInfo, String signature) {
      Purchase purchase = new Json().fromJson(Purchase.class, jsonPurchaseInfo);
      purchase.itemType = itemType;
      purchase.originalJson = jsonPurchaseInfo;
      purchase.signature = signature;
      return purchase;
    }

    public String getItemType() { return itemType; }
    public String getOrderId() { return orderId; }
    public String getPackageName() { return packageName; }
    public String getSku() { return sku; }
    public long getPurchaseTime() { return purchaseTime; }
    public int getPurchaseState() { return purchaseState; }
    public String getDeveloperPayload() { return developerPayload; }
    public String getToken() { return token; }
    public String getOriginalJson() { return originalJson; }
    public String getSignature() { return signature; }

    @Override
    public String toString() { return "PurchaseInfo(type:" + itemType + "):" + originalJson; }
}
