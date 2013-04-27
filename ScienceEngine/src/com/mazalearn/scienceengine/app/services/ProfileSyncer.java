package com.mazalearn.scienceengine.app.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mazalearn.scienceengine.app.services.ProfileData.Social;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.utils.ProfileMapConverter;

public class ProfileSyncer {
  
  public static final long FORCED_SYNC = -1L;
  private static long testCurrentTime = 0L;
  private static List<String> serverProfileItems = 
      Arrays.asList(new String[] {ProfileData.SERVER_PROPS, ProfileData.SOCIAL});
  
  public static void setTestCurrentTime(long ttestCurrentTime) {
    testCurrentTime = ttestCurrentTime;
  }
  
  public long getCurrentTime() { // Refactored out for testing
    return testCurrentTime == 0 ? System.currentTimeMillis() : testCurrentTime;
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
  
  public Map<String, Object> doSync(Map<String, Object> myData,
      Map<String, Object> yourData, Map<String, Long> myTimestamps,
      Map<String, Long> yourTimestamps) {
    syncMerge(myTimestamps, yourTimestamps, myData, yourData);
    // All sync time stamps for non-null values >= your time stamps at this point
    
    // TODO: why below line??? should only be on client with unreliable time
    // myTimestamps.put(ProfileData.LAST_SYNC_TIME, yourTimestamps.get(ProfileData.THIS_SYNC_TIME));
    
    // Special case: When no timestamps from client, should force sync topicStats as well
    if (yourTimestamps.size() == 0) {
      yourTimestamps.put(ProfileData.TOPIC_STATS, FORCED_SYNC);
    }
    Map<String, Long> syncTimestamps = new HashMap<String, Long>();
    Map<String, Object> syncData = getSyncData(myTimestamps, yourTimestamps, myData, syncTimestamps);
    syncTimestamps.put(ProfileData.THIS_SYNC_TIME, 
        nvl(yourTimestamps.get(ProfileData.THIS_SYNC_TIME), getCurrentTime()));
    
    syncData.put(ProfileData.LAST_UPDATED, syncTimestamps);
    return syncData;
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
    if (yourTimestamps == null) {
      yourData.put(ProfileData.LAST_UPDATED, new HashMap<String, Long>());
      yourTimestamps = (Map<String, Long>) yourData.get(ProfileData.LAST_UPDATED);
    }

    // First sync social data
    if (syncSocialClient(serverData.social, clientData.social)) {
      myTimestamps.put(ProfileData.SOCIAL, getCurrentTime());
    }
    
   // merge server profile on top of this
    syncMerge(myTimestamps, yourTimestamps, myData, yourData);
    // All my time stamps >= your time stamps at this point
    clientData.serverTimestamps = yourTimestamps;
    
    Long lastSyncTime = nvl(yourTimestamps.get(ProfileData.THIS_SYNC_TIME), 0);
    myTimestamps.put(ProfileData.LAST_SYNC_TIME, lastSyncTime);
    // Remove timestamps older than last sync time for client side items
    for(Iterator<Map.Entry<String, Long>> it = myTimestamps.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, Long> entry = it.next();
      if(nvl(entry.getValue(), 0) < lastSyncTime && !serverProfileItems.contains(entry.getKey())) {
        it.remove();
      }
    }
    
    ProfileMapConverter.mapToProfile(clientData, myData);
  }

  /**
   * 
   * @param serverSocial
   * @param clientSocial
   * @return true iff serversocial had something which caused a change in client social
   */
  public static boolean syncSocialClient(Social serverSocial, Social clientSocial) {
    // Get inbox mq of server into local inbox
    boolean changed = false;
    if (serverSocial != null) {
      for (Message msg: serverSocial.inbox.mq) {        if (msg.messageId <= clientSocial.inbox.headId) continue;
        clientSocial.inbox.addMessage(msg);
        clientSocial.inbox.headId = Math.max(clientSocial.inbox.headId, msg.messageId);
        changed = true;
      }
      clientSocial.inbox.tailId = clientSocial.inbox.tailId;
      clientSocial.outbox.headId = serverSocial.outbox.headId;
      // Remove outbox messages consumed at server and send the rest
      for (int i = clientSocial.outbox.mq.size() - 1; i >= 0; i--) {
        Message msg = clientSocial.outbox.mq.get(i);
        if (msg.messageId < serverSocial.outbox.headId) {
          clientSocial.outbox.mq.remove(msg);
          changed = true;
        }
      }
      // sync friends list
      for (String email: serverSocial.friends) {
        if (!clientSocial.friends.contains(email)) {
          clientSocial.friends.add(email);
          changed = true;
        }
      }
    }
    return changed;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> getSyncJson(ProfileData clientData) {
    Map<String, Object> myData = ProfileMapConverter.profileToMap(clientData);
    Map<String, Long> myTimestamps = (Map<String, Long>) myData.get(ProfileData.LAST_UPDATED);
    
    Map<String, Long> yourTimestamps = 
        clientData.serverTimestamps != null ? clientData.serverTimestamps : new HashMap<String, Long>();
    // always sync below topic stats
    myTimestamps.put(ProfileData.TOPIC_STATS, 0L);
    yourTimestamps.put(ProfileData.TOPIC_STATS, FORCED_SYNC);
    
    Map<String, Long> syncTimestamps = new HashMap<String, Long>();
    Map<String, Object> syncData = getSyncData(myTimestamps, yourTimestamps, myData, syncTimestamps);
    syncData.put(ProfileData.LAST_UPDATED, syncTimestamps);
    syncTimestamps.put(ProfileData.THIS_SYNC_TIME, getCurrentTime());
    // Add server item sync times, in case they are not there to prevent server sending them
    for (String key: serverProfileItems) {
      syncTimestamps.put(key, myTimestamps.get(key));
    }
    return syncData;
  }
  
}
