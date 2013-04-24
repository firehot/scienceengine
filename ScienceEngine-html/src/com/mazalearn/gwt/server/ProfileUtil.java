package com.mazalearn.gwt.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.services.ProfileData.Social;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;

public class ProfileUtil {

  private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
  private JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();
  
  public static EmbeddedEntity createOrGetUserProfile(Entity user, boolean create) {
    if (user == null) return null;
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(ProfileServlet.PROFILE);
    if (create && profileEntity == null) {
      System.out.println("Creating profile for user:" + user.getKey().getName());
      profileEntity = new EmbeddedEntity();
      profileEntity.setProperty(ProfileData.LAST_UPDATED, new EmbeddedEntity());
      profileEntity.setProperty(ProfileData.TOPIC_STATS, new EmbeddedEntity());
      user.setProperty(ProfileServlet.PROFILE, profileEntity);
    }
    return profileEntity;
  }

  public Entity createOrGetUser(String userId, boolean create) {
    if (userId == null || userId.length() == 0) return null;
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userId.toLowerCase());
    Entity user = null, user1 = null;
    try {
      user = ds.get(key);
      String profileId = (String) user.getProperty(ProfileServlet.NEW_USER_ID);
      if (profileId != null && profileId.length() > 0) {
        key = KeyFactory.createKey(User.class.getSimpleName(), profileId);
        user1 = ds.get(key);
        user = user1;
      }
    } catch (EntityNotFoundException e) {
      if (create && user == null) {
        System.out.println("Creating user:" + userId.toLowerCase());
        user = new Entity(User.class.getSimpleName(), userId.toLowerCase());
        ds.put(user);
      }
    }
    return user;
  }

  // clientprofile may be null for a GET request
  public String getUserSyncProfileAsBase64(String userId, ProfileData clientProfile) 
      throws IllegalStateException {
    EmbeddedEntity serverProfile = retrieveUserProfile(userId);
    if (serverProfile == null) {
      System.out.println("No user profile: " + userId);
      return "";
    }
    
    String json = getUserSyncProfile(serverProfile, clientProfile);
    
    String profileStringBase64 = Base64.encode(json);
    return profileStringBase64;
  }

  // Package protected for testing
  // clientprofile may be null for a GET request
  String getUserSyncProfile(EmbeddedEntity serverProfile, ProfileData clientProfile) 
      throws IllegalStateException {
    System.out.println("getUserSyncProfile serverprofile: " + serverProfile);
    EmbeddedEntity serverUpdates = (EmbeddedEntity) serverProfile.getProperty(ProfileData.LAST_UPDATED);
    Map<String, Long> clientUpdates = 
        clientProfile != null ? clientProfile.lastUpdated : new HashMap<String, Long>();
    long clientDefault = nvl(clientUpdates.get(ProfileData.LAST_SYNC_TIME), 0L);
    StringBuilder json = new StringBuilder("{");
    String topicDelimiter = "";
    for (Map.Entry<String, Object> property: serverProfile.getProperties().entrySet()) {
      String key = property.getKey();
      Object value = property.getValue();
      // If server version is older than client version, no need to send
      if (nvl((Long)serverUpdates.getProperty(key), 1) <= nvl(clientUpdates.get(key), clientDefault)) continue;
      if (value instanceof Text) {
        String s = (value == null) ? "{}" : ((Text) value).getValue();
        if (!"null".equals(s)) {
          json.append(topicDelimiter + key + ":" + s);
          topicDelimiter = ",";
        }
      } else if (value instanceof EmbeddedEntity) {
        EmbeddedEntity embeddedEntity = (EmbeddedEntity) value;
        json.append(topicDelimiter + key + ":{");
        topicDelimiter = ",";
        String delimiter = "";
        for (Map.Entry<String, Object> p: embeddedEntity.getProperties().entrySet()) {
          Object v = p.getValue();
          // If server version is older than client version, no need to send
          if (v instanceof Text) {
            if (nvl((Long)serverUpdates.getProperty(p.getKey()), 1) <= nvl(clientUpdates.get(p.getKey()), clientDefault)) continue;
            String s = (v == null) ? "{}" : ((Text) v).getValue();
            if (!"null".equals(s)) {
              json.append(delimiter + p.getKey() + ":" + s);
              delimiter = ",";
            }
          } else if (v instanceof Long) {
            json.append(delimiter + p.getKey() + ":" + v);
            delimiter = ",";
          }
        }
        json.append("}");
      }
    }
    
    json.append("}");
    System.out.println("getUserSyncProfile: " + json);
    return json.toString();
  }

  public EmbeddedEntity retrieveUserProfile(String userId) {
    Entity user = createOrGetUser(userId, false);
    return createOrGetUserProfile(user, false);
  }

  public Entity retrieveUser(String userId) {
    return createOrGetUser(userId, false);
  }

  String saveUserProfile(String userId, ProfileData clientProfile) 
      throws IllegalStateException {
    Entity user = createOrGetUser(userId, true);
    
    EmbeddedEntity serverProfile = createOrGetUserProfile(user, true);
    
    Map<String, Object> props = new HashMap<String, Object>();
    props.put(ProfileData.CLIENT_PROPS, clientProfile.client);
    props.put(ProfileData.COACH_PNG, clientProfile.coachPng);
    props.put(ProfileData.COACH_PNG, clientProfile.userPng);
    
    // Last updated
    EmbeddedEntity serverProfileLastUpdated = (EmbeddedEntity) serverProfile.getProperty(ProfileData.LAST_UPDATED);
    // Client Properties
    merge(serverProfile, props, serverProfileLastUpdated, clientProfile.lastUpdated);
    
    // TopicStats
    EmbeddedEntity topicStatsEntity = (EmbeddedEntity) serverProfile.getProperty(ProfileData.TOPIC_STATS);
    merge(topicStatsEntity, clientProfile.topicStats, serverProfileLastUpdated, clientProfile.lastUpdated);    
    
    // Social
    syncSocial(userId, clientProfile, serverProfile);
    ds.put(user);
    // If retrieved user is for requested userid and not forwarded
    if (userId.equals(user.getKey().getName())) {
      return (String) user.getProperty(ProfileServlet.OLD_USER_ID);
    }
    return null;
  }

  public ProfileData profileFromBase64(byte[] profileBytes) {
    return jsonEntityUtil.profileFromBase64(profileBytes);
  }

  private void syncSocial(String userId, ProfileData clientProfile,
      EmbeddedEntity serverProfile) {
    if (clientProfile.social == null) return;
    // extract server profile social
    Social serverSocial = jsonEntityUtil.getFromJsonTextProperty(serverProfile, ProfileData.SOCIAL, Social.class);
    Social clientSocial = clientProfile.social;
    // Clear out all outbox messages from client
    for (Message msg: clientSocial.outbox) {
      // If message has already been processed, ignore it.
      if (msg.messageId < serverSocial.lastOutboxMessageId) continue;
      String toEmail = msg.email;
      Entity toUser = createOrGetUser(toEmail, true);
      EmbeddedEntity toUserProfile = createOrGetUserProfile(toUser, true);
      // If toUser has no installation, send an email invite
      if (toUserProfile.getProperty(ProfileData.INSTALL_ID) == null) {
        EmailUtil.sendUserInvite(userId, toEmail);
      }
      // add gift to inbox of toUser's social
      // TODO: We should ideally lock toUser but we are being careless here
      Social toSocial = jsonEntityUtil.getFromJsonTextProperty(toUserProfile, ProfileData.SOCIAL, Social.class); 
      System.out.println("Transferring Gift " + msg.messageId + " to " + toEmail);
      msg.messageId = toSocial.lastInboxMessageId++;
      msg.email = userId;
      toSocial.inbox.add(msg);
      jsonEntityUtil.setAsJsonTextProperty(toUserProfile, ProfileData.SOCIAL, toSocial);
      ds.put(toUser);
    }
    serverSocial.lastOutboxMessageId = clientSocial.lastOutboxMessageId;
    // Merge in friends list - for now copy over from client.
    serverSocial.friends = clientSocial.friends;
    
    jsonEntityUtil.setAsJsonTextProperty(serverProfile, ProfileData.SOCIAL, serverSocial);
  }

  private void merge(PropertyContainer serverEntity, Map<String, ?> props, EmbeddedEntity serverUpdates,
      Map<String, Long> clientUpdates) {
    for (Map.Entry<String, ?> entry: props.entrySet()) {
      String key = entry.getKey();
      long clientLastUpdated = ProfileUtil.nvl(clientUpdates.get(key), 0L);
      long serverLastUpdated = ProfileUtil.nvl((Long)serverUpdates.getProperty(key), 0L);
      if ( clientLastUpdated > serverLastUpdated) {
        jsonEntityUtil.setAsJsonTextProperty(serverEntity, key, entry.getValue());
        serverUpdates.setProperty(key, clientLastUpdated);
      }    
    }
  }

  public void confirmRegistrationInfo(String userEmail, String installId,
      String userName, EmbeddedEntity newUserProfile,
      Entity oldUser) {
    EmbeddedEntity oldUserProfile = createOrGetUserProfile(oldUser, true);
    ServerProps serverProps = jsonEntityUtil.getFromJsonTextProperty(oldUserProfile, ProfileData.SERVER_PROPS, ServerProps.class);
    serverProps.userName = userName;
    serverProps.userId = userEmail;
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    serverProps.registrationDate = dateFormat.format(date);
    jsonEntityUtil.setAsJsonTextProperty(oldUserProfile, ProfileData.SERVER_PROPS, serverProps);
    oldUserProfile.setProperty(ProfileServlet.PROFILE, newUserProfile);
    
    Entity newUser = createOrGetUser(userEmail, true);
    newUser.setPropertiesFrom(oldUser);
  
    newUser.setProperty(ProfileServlet.OLD_USER_ID, installId);
    ds.put(newUser);
    oldUser.setProperty(ProfileServlet.NEW_USER_ID, userEmail);
    ds.put(oldUser);
  }

  public void saveRegistrationInfo(Entity user, EmbeddedEntity profile,
      String sex, String grade, String school,
      String city, String comments) {
    ServerProps serverProps = jsonEntityUtil.getFromJsonTextProperty(profile, ProfileData.SERVER_PROPS, ServerProps.class);
    serverProps.sex = sex;
    serverProps.grade = grade;
    serverProps.school = school;
    serverProps.city = city;
    serverProps.comments = comments;
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    serverProps.registrationDate = dateFormat.format(date);
    jsonEntityUtil.setAsJsonTextProperty(profile, ProfileData.SERVER_PROPS, serverProps);
    
    ds.put(user);
  }

  private static long nvl(Long value, long defaultValue) {
    return value == null ? defaultValue : value;
  }

  public void deleteUserProfile(String userEmail) throws IllegalStateException {
    Entity user = retrieveUser(userEmail);
    if (user != null) {
      user.setProperty(ProfileServlet.PROFILE, null);
      ds.put(user);      
    }
  }

  public void deleteOldUser(String userId, String oldUserId) {
    Key key = KeyFactory.createKey(User.class.getSimpleName(), oldUserId.toLowerCase());
    ds.delete(key);
    System.out.println("Deleted: " + oldUserId);
    Entity newUser = retrieveUser(userId);
    newUser.removeProperty(ProfileServlet.OLD_USER_ID);
    ds.put(newUser);
  }


}
