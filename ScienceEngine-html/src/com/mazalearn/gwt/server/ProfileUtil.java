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

  public String getUserSyncProfileAsBase64(String userId) 
      throws IllegalStateException {
    EmbeddedEntity profileEntity = retrieveUserProfile(userId);
    if (profileEntity == null) {
      System.out.println("No user profile: " + userId);
      return "";
    }
    
    System.out.println("getUserProfileAsBase64: " + profileEntity);
    
    StringBuilder json = new StringBuilder("{");
    String topicDelimiter = "";
    for (Map.Entry<String, Object> property: profileEntity.getProperties().entrySet()) {
      Object value = property.getValue();
      if (value instanceof Text) {
        String v = (value == null) ? "{}" : ((Text) value).getValue();
        if (!"null".equals(v)) {
          json.append(topicDelimiter + property.getKey() + ":" + v);
          topicDelimiter = ",";
        }
      } else if (value instanceof EmbeddedEntity) {
        EmbeddedEntity embeddedEntity = (EmbeddedEntity) value;
        json.append(topicDelimiter + property.getKey() + ":{");
        topicDelimiter = ",";
        String delimiter = "";
        for (Map.Entry<String, Object> p: embeddedEntity.getProperties().entrySet()) {
          Object v = p.getValue();
          if (value instanceof Text) {
            String s = (v == null) ? "{}" : ((Text) v).getValue();
            if (!"null".equals(v)) {
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
    System.out.println("getUserProfileAsBase64: " + json);
    String profileStringBase64 = Base64.encode(json.toString());
    return profileStringBase64;
  }

  public EmbeddedEntity retrieveUserProfile(String userId) {
    Entity user = createOrGetUser(userId, false);
    return createOrGetUserProfile(user, false);
  }

  public Entity retrieveUser(String userId) {
    return createOrGetUser(userId, false);
  }

  String saveUserProfile(String userId, byte[] profileBytes) 
      throws IllegalStateException {
    Entity user = createOrGetUser(userId, true);
    String clientProfileBase64 = new String(profileBytes);
    String clientProfileJson = new String(Base64.decode(clientProfileBase64));
    
    // Trim at end where 0 chars are present.
    int count = clientProfileJson.length();
    while (clientProfileJson.charAt(--count) == 0);
    System.out.println("saveUserProfile:" + clientProfileJson.substring(0, count + 1));
    // Get profile data and sync with existing one
    ProfileData clientProfile = jsonEntityUtil.profileFromJson(clientProfileJson.substring(0, count+1));
    
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
    ds.put(user);
    // If retrieved user is for requested userid and not forwarded
    if (userId.equals(user.getKey().getName())) {
      return (String) user.getProperty(ProfileServlet.OLD_USER_ID);
    }
    return null;
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


}
