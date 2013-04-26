package com.mazalearn.gwt.server;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.services.ProfileData.Social;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.services.ProfileSyncer;
import com.mazalearn.scienceengine.app.utils.ProfileMapConverter;

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

  static private class TextSerializer implements JsonSerializer<Text> {
    JsonParser jsonParser = new JsonParser();
    @Override
    public JsonElement serialize(Text src, Type typeOfSrc,
        JsonSerializationContext context) {
      return jsonParser.parse(src.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  public String getUserSyncProfile(String userEmail, EmbeddedEntity serverProfile, ProfileData clientProfile) 
      throws IllegalStateException {
    ProfileSyncer profileSyncer = new ProfileSyncer();  
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(Text.class, new TextSerializer())
        .create();

    // Always do a Social sync - if client sends social data
    boolean socialSyncDone = false;
    if (clientProfile.social != null) {
      Social serverSocial = jsonEntityUtil.getFromJsonTextProperty(serverProfile, ProfileData.SOCIAL, Social.class);
      syncSocialServer(userEmail, clientProfile.social, serverSocial);
      jsonEntityUtil.setAsJsonTextProperty(serverProfile, ProfileData.SOCIAL, serverSocial);
      socialSyncDone = true;
    }
    
    EntityMapConverter entityMapConverter = new EntityMapConverter();
    // Sync other parts of profile
    Map<String, Object> myData = entityMapConverter.entityToMap(serverProfile);
    Map<String, Object> yourData = ProfileMapConverter.profileToMap(clientProfile);
    Map<String, Long> myTimestamps = (Map<String, Long>) myData.get(ProfileData.LAST_UPDATED);
    Map<String, Long> yourTimestamps = (Map<String, Long>) yourData.get(ProfileData.LAST_UPDATED);
    
    // set SOCIAL timestamp to now if social sync was done -otherwise, it can be what it was already set to
    if (socialSyncDone) {
      myTimestamps.put(ProfileData.SOCIAL, profileSyncer.getCurrentTime());
    }
    
    // If there is no data on client - do a forced sync
    if (yourTimestamps.size() == 0) {
      yourTimestamps.put(ProfileData.TOPIC_STATS, -1L);
    }
    String syncJson = profileSyncer.doSync(gson, myData, yourData, myTimestamps, yourTimestamps);
        
    // save data back from map into entity profile
    entityMapConverter.mapToEntity(serverProfile, myData);
    
    // TODO: only inbox of client needs to be pushed if it has changed
    // syncData.put(ProfileData.SOCIAL, serverSocial);
    
    return syncJson;
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
    if (serverProfile == null) {
      System.out.println("No user profile: " + userId);
      return "";
    }
    
    String syncProfileStr = getUserSyncProfile(userId, serverProfile, clientProfile);
    
    // If retrieved user is for requested userid and not forwarded
    if (userId.equals(user.getKey().getName())) {
      String oldUserId = (String) user.getProperty(ProfileServlet.OLD_USER_ID);
      if (oldUserId != null) {
        Key key = KeyFactory.createKey(User.class.getSimpleName(), oldUserId.toLowerCase());
        ds.delete(key);
        System.out.println("Deleted: " + oldUserId);
        user.removeProperty(ProfileServlet.OLD_USER_ID);
      }
    }
    ds.put(user);
    System.out.println(syncProfileStr);
    return Base64.encode(syncProfileStr);
  }

  public ProfileData profileFromBase64(byte[] profileBytes) {
    return jsonEntityUtil.profileFromBase64(profileBytes);
  }

  /**
   * 
   * @param fromEmail - email of current user to be used to send invites
   * @param clientSocial
   * @param serverSocial
   */
  private void syncSocialServer(String fromEmail, Social clientSocial, Social serverSocial) {
    // Consume all outbox messages from client
    for (Message msg: clientSocial.outbox.mq) {
      // If message has already been processed, ignore it.
      // TODO: serverSocial should maintain outboxmessageid per installation per user
      if (msg.messageId < serverSocial.outbox.headId) continue;
      String toEmail = msg.email;
      String installId = null;

      Transaction txn = ds.beginTransaction();
      try {
        Entity toUser = createOrGetUser(toEmail, true);
        EmbeddedEntity toUserProfile = createOrGetUserProfile(toUser, true);
        installId = (String) toUserProfile.getProperty(ProfileData.INSTALL_ID);
        // If toUserProfile already exists and is bound to an installation, add gift to inbox.
        if (installId != null) {
          // Update inbox with gift
          Social toSocial = jsonEntityUtil.getFromJsonTextProperty(toUserProfile, ProfileData.SOCIAL, Social.class); 
          System.out.println("Transferring Gift " + msg.messageId + " to " + toEmail);
          msg.email = fromEmail;
          toSocial.inbox.addMessage(msg);
          jsonEntityUtil.setAsJsonTextProperty(toUserProfile, ProfileData.SOCIAL, toSocial);
          
          // update timestamp of social properties which have now been updated
          EmbeddedEntity lastUpdated = (EmbeddedEntity) toUserProfile.getProperty(ProfileData.LAST_UPDATED);
          lastUpdated.setProperty(ProfileData.SOCIAL, System.currentTimeMillis());
          
          // save user and commit transaction
          ds.put(toUser);
          txn.commit();
        }
      } finally {
          if (txn.isActive()) {
              txn.rollback();
          }
      }
      
      // If toUser has no installation, send an email invite
      if (installId == null) {
        EmailUtil.sendUserInvite(fromEmail, toEmail);
      }
    }
    serverSocial.outbox.headId = clientSocial.outbox.tailId;
    serverSocial.outbox.tailId = clientSocial.outbox.tailId;
    serverSocial.outbox.mq.clear();
    
    // Remove inbox messages consumed at client and send the rest
    for (int i = serverSocial.inbox.mq.size() - 1; i >= 0; i--) {
      Message msg = serverSocial.inbox.mq.get(i);
      if (msg.messageId < clientSocial.inbox.headId) {
        serverSocial.inbox.mq.remove(msg);
      }
    }
    // Merge in friends list - for now copy over from client.
    serverSocial.friends = clientSocial.friends;
  }

  public void confirmRegistrationInfo(String userEmail, String installId,
      String userName, EmbeddedEntity newUserProfile,
      Entity oldUser) {
    EmbeddedEntity oldUserProfile = createOrGetUserProfile(oldUser, true);
    ServerProps serverProps = jsonEntityUtil.getFromJsonTextProperty(oldUserProfile, ProfileData.SERVER_PROPS, ServerProps.class);
    serverProps.userName = userName;
    serverProps.userId = userEmail;
    serverProps.isRegistered = true;
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    serverProps.registrationDate = dateFormat.format(date);
    jsonEntityUtil.setAsJsonTextProperty(oldUserProfile, ProfileData.SERVER_PROPS, serverProps);
    // update timestamp of server properties which have now been updated
    EmbeddedEntity lastUpdated = (EmbeddedEntity) oldUserProfile.getProperty(ProfileData.LAST_UPDATED);
    lastUpdated.setProperty(ProfileData.SERVER_PROPS, System.currentTimeMillis());
    
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
    // update timestamp of server properties which have now been updated
    EmbeddedEntity lastUpdated = (EmbeddedEntity) profile.getProperty(ProfileData.LAST_UPDATED);
    lastUpdated.setProperty(ProfileData.SERVER_PROPS, System.currentTimeMillis());
    
    ds.put(user);
  }

  public void deleteUserProfile(String userEmail) throws IllegalStateException {
    Entity user = retrieveUser(userEmail);
    if (user != null) {
      user.setProperty(ProfileServlet.PROFILE, null);
      ds.put(user);      
    }
  }
}
