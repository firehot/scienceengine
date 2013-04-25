package com.mazalearn.gwt.server;

import java.util.HashMap;
import java.util.Map;

import com.mazalearn.scienceengine.app.services.ProfileData;

public class ProfileSyncer {
  
  /*
   * Merge your data in to mine overwriting any item for which you have a later timestamp
   * 
   * function syncMerge(myTimestamps, yourTimestamps, myData, yourData):
        for each key in yourData do
          if yourData[key] is a Dictionary then
            syncMerge(myTimestamps, yourTimestamps, myData[key], yourData[key])
          else if myTimestamps[key] < youtTimestamps[key] then
            myData[key] := yourData[key]
            myTimestamps[key] := yourTimestamps[key]
          fi
        od
   */
  
  @SuppressWarnings("unchecked")
  void syncMerge(Map<String, Long> myTimestamps, Map<String, Long> yourTimestamps,
      Map<String, Object> myData, Map<String, Object> yourData) {
    
    for (Map.Entry<String, Long> entry: yourTimestamps.entrySet()) {
      String key = entry.getKey();
      if (key.equals(ProfileData.LAST_UPDATED) || yourData.get(key) == null) continue;
      Long yourTimestampForKey = entry.getValue();
      if (yourData.get(key) instanceof Map) {
        syncMerge(myTimestamps, yourTimestamps, 
            (Map<String, Object>) myData.get(key), (Map<String, Object>) yourData.get(key));
      } else if (yourTimestampForKey > nvl(myTimestamps.get(key), 0)) {
        myData.put(key, yourData.get(key));
        myTimestamps.put(key, yourTimestampForKey);
      }
    }        
  }
  
  /*
   * Send me your timestamps and lastsync time.
   * If for any object, your timestamp is older than mine, I will send it to you.
   * Problem: Have to send all timestamps every time. 
   * So for nested syncs, we send back data only if requested -
   *    by setting a timestamp for the nested item collection.
   * 
   *  function getSyncData(myTimestamps, myData, yourTimestamps):
        Dictionary dataToSend := { “lastSyncTime”: lastSyncTime }
        for each key in myTimestamps do
          if myData[key] is a Dictionary && yourTimestamps[key] == 1 then
            dataToSend += syncSend(myTimestamps, myData[key], yourTimestamps)
          else if myTimestamps[key] > yourTimestamp[key] then
            dataToSend += {key: firstData[key]}
          fi
        od
        return dataToSend
   */
  @SuppressWarnings("unchecked")
  Map<String, Object> getSyncData(Map<String, Long> myTimestamps,
      Map<String, Long> yourTimestamps, Map<String, Object> myData,
      Map<String, Long> syncTimestamps) {
    Map<String, Object> syncData = new HashMap<String, Object>();
    
    for (Map.Entry<String, Long> entry: myTimestamps.entrySet()) {
      String key = entry.getKey();
      if (key.equals(ProfileData.LAST_UPDATED) || myData.get(key) == null) continue;
      Long myTimestampForKey = entry.getValue();
      if (myData.get(key) instanceof Map) {
        if (yourTimestamps.get(key) != null) {
          Map<String, Object> entrySyncData = 
              getSyncData(myTimestamps, yourTimestamps, (Map<String, Object>) myData.get(key), syncTimestamps);
          syncData.put(key, entrySyncData);
        }
      } else if (myTimestampForKey > nvl(yourTimestamps.get(key), 0)) {
        syncData.put(key, myData.get(key));
        syncTimestamps.put(key, myTimestampForKey);
      }
    }
    return syncData;
  }
  
  private static long nvl(Long value, long defaultValue) {
    return value == null ? defaultValue : value;
  }

}
