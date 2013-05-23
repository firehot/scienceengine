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
    String mItemType;
    String mSku;
    String mType;
    String mPrice;
    String mTitle;
    String mDescription;
    String mJson;

    public SkuDetails() {}
    
    public SkuDetails(String title, String description, String price) {
      mSku = mTitle = title;
      mDescription = description;
      mPrice = price;
    }
    
    public static SkuDetails toSkuDetails(String itemType, String jsonSkuDetails) {
      SkuDetails skuDetails = new Json().fromJson(SkuDetails.class, jsonSkuDetails);
      skuDetails.mItemType = itemType;
      skuDetails.mJson = jsonSkuDetails;
      return skuDetails;
    }

    public String getSku() { return mSku; }
    public String getType() { return mType; }
    public String getPrice() { return mPrice; }
    public String getTitle() { return mTitle; }
    public String getDescription() { return mDescription; }

    @Override
    public String toString() {
        return "SkuDetails:" + mJson;
    }
}
