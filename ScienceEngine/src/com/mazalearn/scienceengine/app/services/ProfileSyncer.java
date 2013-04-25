package com.mazalearn.scienceengine.app.services;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.ProfileData.Social;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.utils.ProfileMapConverter;

public class ProfileSyncer {
  
  public static final long FORCED_SYNC = -1L;

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
  public void syncMerge(Map<String, Long> myTimestamps, Map<String, Long> yourTimestamps,
      Map<String, Object> myData, Map<String, Object> yourData) {
    
    for (Map.Entry<String, Long> entry: yourTimestamps.entrySet()) {
      String key = entry.getKey();
      if (key.equals(ProfileData.LAST_UPDATED) || yourData.get(key) == null) continue;
      Long yourTimestampForKey = entry.getValue();
      if (myData.get(key) instanceof Map) {
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
          if myData[key] is a Dictionary then
            if yourTimestamps[key] == 0 then
               dataToSend += syncSend(myTimestamps, myData[key], yourTimestamps)
            fi
          else if myTimestamps[key] > yourTimestamp[key] then
            dataToSend += {key: firstData[key]}
          fi
        od
        return dataToSend
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getSyncData(Map<String, Long> myTimestamps,
      Map<String, Long> yourTimestamps, Map<String, Object> myData,
      Map<String, Long> syncTimestamps) {
    Map<String, Object> syncData = new HashMap<String, Object>();
    
    for (Map.Entry<String, Long> entry: myTimestamps.entrySet()) {
      String key = entry.getKey();
      if (key.equals(ProfileData.LAST_UPDATED) || myData.get(key) == null) continue;
      Long myTimestampForKey = entry.getValue();
      if (myData.get(key) instanceof Map && nvl(yourTimestamps.get(key), 0) == FORCED_SYNC) {
          Map<String, Object> entrySyncData = 
              getSyncData(myTimestamps, yourTimestamps, (Map<String, Object>) myData.get(key), syncTimestamps);
          syncData.put(key, entrySyncData);
      } else if (myTimestampForKey > nvl(yourTimestamps.get(key), 0)) {
        syncData.put(key, myData.get(key));
        syncTimestamps.put(key, myTimestampForKey);
      }
    }
    return syncData;
  }
  
  public String doSync(Gson gson, Map<String, Object> myData,
      Map<String, Object> yourData, Map<String, Long> myTimestamps,
      Map<String, Long> yourTimestamps) {
    syncMerge(myTimestamps, yourTimestamps, myData, yourData);
    // All first time stamps >= second time stamps at this point
    
    // TODO: why below line??? should only be on client with unreliable time
    // myTimestamps.put(ProfileData.LAST_SYNC_TIME, yourTimestamps.get(ProfileData.THIS_SYNC_TIME));
    
    // Special case: When no timestamps from client, should force sync topicStats as well
    if (yourTimestamps.size() == 0) {
      yourTimestamps.put(ProfileData.TOPIC_STATS, FORCED_SYNC);
    }
    Map<String, Long> syncTimestamps = new HashMap<String, Long>();
    Map<String, Object> syncData = getSyncData(myTimestamps, yourTimestamps, myData, syncTimestamps);
    syncData.put(ProfileData.LAST_UPDATED, syncTimestamps);
    String syncJson = gson.toJson(syncData);
    return syncJson;
  }

  private static long nvl(Long value, long defaultValue) {
    return value == null ? defaultValue : value;
  }

  @SuppressWarnings("unchecked")
  public void mergeProfile(ProfileData serverData, ProfileData clientData) {
    Map<String, Object> yourData = ProfileMapConverter.profileToMap(serverData);
    Map<String, Object> myData = ProfileMapConverter.profileToMap(clientData);
    Map<String, Long> myTimestamps = (Map<String, Long>) myData.get(ProfileData.LAST_UPDATED);
    Map<String, Long> yourTimestamps = (Map<String, Long>) yourData.get(ProfileData.LAST_UPDATED);
    
   // Other profile is later - merge other on top of this
    syncMerge(myTimestamps, yourTimestamps, myData, yourData);
    // All first time stamps >= second time stamps at this point
    
    // TODO: why below line??? should only be on client with unreliable time
    // myTimestamps.put(ProfileData.LAST_SYNC_TIME, yourTimestamps.get(ProfileData.THIS_SYNC_TIME));
    syncSocialClient(serverData.social, clientData.social);
    
    ProfileMapConverter.mapToProfile(clientData, myData);
    clientData.lastUpdated.put(ProfileData.LAST_SYNC_TIME, System.currentTimeMillis());
  }

  private static void syncSocialClient(Social serverSocial, Social clientSocial) {
    // Get inbox messages from server into local inbox
    if (serverSocial != null) {
      for (Message msg: serverSocial.inbox) {
        if (msg.messageId < serverSocial.lastInboxMessageId) continue;
        clientSocial.inbox.add(msg);
        clientSocial.lastInboxMessageId = Math.max(clientSocial.lastInboxMessageId, msg.messageId);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public String getSyncJson(ProfileData clientData) {
    Map<String, Object> myData = ProfileMapConverter.profileToMap(clientData);
    Map<String, Long> myTimestamps = (Map<String, Long>) myData.get(ProfileData.LAST_UPDATED);
    
    Map<String, Long> yourTimestamps = new HashMap<String, Long>();
    // no need to sync server props
    yourTimestamps.put(ProfileData.SERVER_PROPS, System.currentTimeMillis());
    // always sync below topic stats
    myTimestamps.put(ProfileData.TOPIC_STATS, 0L);
    yourTimestamps.put(ProfileData.TOPIC_STATS, FORCED_SYNC);
    
    Map<String, Long> syncTimestamps = new HashMap<String, Long>();
    Map<String, Object> syncData = getSyncData(myTimestamps, yourTimestamps, myData, syncTimestamps);
    syncData.put(ProfileData.LAST_UPDATED, syncTimestamps);
    String syncProfileStr = new Gson().toJson(syncData);
    return syncProfileStr;
  }
  
}
