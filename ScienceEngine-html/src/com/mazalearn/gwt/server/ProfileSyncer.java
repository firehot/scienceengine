package com.mazalearn.gwt.server;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.utils.ProfileMapConverter;

public class ProfileSyncer {

  public long getCurrentTime() { // Refactored out for testing
    return System.currentTimeMillis();
  }
  
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
  
  Map<String, Object> doSync(Map<String, Long> myTimestamps, Map<String, Long> yourTimestamps,
      Map<String, Object> myData, Map<String, Object> yourData, Map<String, Long> syncTimestamps) {
    
    syncMerge(myTimestamps, yourTimestamps, myData, yourData);
    // All first time stamps >= second time stamps at this point
    // why below line??? should only be on client with unreliable time
    myTimestamps.put(ProfileData.LAST_SYNC_TIME, yourTimestamps.get(ProfileData.THIS_SYNC_TIME));
    
    Map<String, Object> syncData = getSyncData(myTimestamps, yourTimestamps, myData, syncTimestamps);

    myTimestamps.put(ProfileData.THIS_SYNC_TIME, getCurrentTime());
    syncData.put(ProfileData.LAST_UPDATED, syncTimestamps);
    return syncData;
  }
  
  static private class TextSerializer implements JsonSerializer<Text> {
    JsonParser jsonParser = new JsonParser();
    @Override
    public JsonElement serialize(Text src, Type typeOfSrc,
        JsonSerializationContext context) {
      return jsonParser.parse(src.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  public String getUserSyncProfile(EmbeddedEntity serverProfile, ProfileData clientProfile) 
      throws IllegalStateException {
    Map<String, Object> myData = EntityMapConverter.entityToMap(serverProfile);
    Map<String, Object> yourData = ProfileMapConverter.profileToMap(clientProfile);
    Map<String, Long> myTimestamps = (Map<String, Long>) myData.get(ProfileData.LAST_UPDATED);
    Map<String, Long> yourTimestamps = (Map<String, Long>) yourData.get(ProfileData.LAST_UPDATED);
    if (yourTimestamps.size() == 0) { // No data on client - do a forced sync
      yourTimestamps.put(ProfileData.TOPIC_STATS, 0L);
    }
    Map<String, Long> syncTimestamps = new HashMap<String, Long>();
    Map<String, Object> syncData = doSync(myTimestamps, yourTimestamps, myData, yourData, syncTimestamps);
    
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Text.class, new TextSerializer())
        .create();
    return gson.toJson(syncData);
  }
  
  private static long nvl(Long value, long defaultValue) {
    return value == null ? defaultValue : value;
  }

}
