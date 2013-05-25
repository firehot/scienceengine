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
 * Represents an in-app product's listing details.
 */
public class SkuDetails {
    String itemType;
    String productId;
    String type;
    String price;
    String title;
    String description;
    String mJson;

    public SkuDetails() {}
    
    private SkuDetails(String productId, String type, String title, String description, String price) {
      this.productId = productId;
      this.title = title;
      this.description = description;
      this.price = price;
      this.type = type;
    }
    
    public static SkuDetails toSkuDetails(String itemType, String jsonSkuDetails) {
      SkuDetails skuDetails = new Json().fromJson(SkuDetails.class, jsonSkuDetails);
      skuDetails.itemType = itemType;
      skuDetails.mJson = jsonSkuDetails;
      return skuDetails;
    }

    public String getProductId() { return productId; }
    public String getType() { return type; }
    public String getPrice() { return price; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "SkuDetails:" + mJson;
    }
}
