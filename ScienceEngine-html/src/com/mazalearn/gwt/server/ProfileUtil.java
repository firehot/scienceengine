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
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.services.ProfileData.Social;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;
import com.mazalearn.scienceengine.app.services.ProfileSyncer;
import com.mazalearn.scienceengine.app.utils.ProfileMapConverter;

public class ProfileUtil {

  private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
  private JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();
  
  public static EmbeddedEntity createOrGetUserProfile(PropertyContainer user, boolean create) {
    if (user == null) return null;
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(ProfileServlet.PROFILE);
    if (create && profileEntity == null) {
      if (user instanceof Entity) {
        System.out.println("Creating profile for user:" + ((Entity) user).getKey().getName());
      }
      profileEntity = new EmbeddedEntity();
      profileEntity.setProperty(ProfileData.LAST_UPDATED, new EmbeddedEntity());
      profileEntity.setProperty(ProfileData.TOPIC_STATS, new EmbeddedEntity());
      user.setProperty(ProfileServlet.PROFILE, profileEntity);
    }
    return profileEntity;
  }

  public PropertyContainer createOrGetUser(String userId, boolean create) {
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
        createOrGetUserProfile(user, true);
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
    Map<String, Object> syncData = profileSyncer.doSync(myData, yourData, myTimestamps, yourTimestamps);
    String syncJson = gson.toJson(syncData);
    
    // save data back from map into entity profile
    entityMapConverter.mapToEntity(serverProfile, myData);
    
    // TODO: only inbox of client needs to be pushed if it has changed
    // syncData.put(ProfileData.SOCIAL, serverSocial);
    
    return syncJson;
  }

  public EmbeddedEntity retrieveUserProfile(String userId) {
    PropertyContainer user = createOrGetUser(userId, false);
    return createOrGetUserProfile(user, false);
  }

  public PropertyContainer retrieveUser(String userId) {
    return createOrGetUser(userId, false);
  }

  String saveUserProfile(String userId, ProfileData clientProfile) 
      throws IllegalStateException {
    PropertyContainer user = createOrGetUser(userId, true);
    EmbeddedEntity serverProfile = createOrGetUserProfile(user, true);
    
    String syncProfileStr = getUserSyncProfile(userId, serverProfile, clientProfile);
    
    // If retrieved user is for requested userid and not forwarded
    if ((user instanceof Entity) && userId.equals(((Entity)user).getKey().getName())) {
      String oldUserId = (String) user.getProperty(ProfileServlet.OLD_USER_ID);
      if (oldUserId != null) {
        Key key = KeyFactory.createKey(User.class.getSimpleName(), oldUserId.toLowerCase());
        ds.delete(key);
        System.out.println("Deleted: " + oldUserId);
        user.removeProperty(ProfileServlet.OLD_USER_ID);
      }
    }
    saveEntity(user);
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
    int newHeadId = serverSocial.outbox.headId;
    for (Message msg: clientSocial.outbox.mq) {
      // If message has already been processed, ignore it.
      // TODO: serverSocial should maintain outboxmessageid per installation per user
      if (msg.messageId < serverSocial.outbox.headId) continue;
      String toEmail = msg.email;
      String installId = null;
      // Messages could be out of order in outbox
      newHeadId = Math.max(newHeadId, msg.messageId);

      Transaction txn = ds.beginTransaction();
      try {
        PropertyContainer toUser = createOrGetUser(toEmail, true);
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
          
          saveEntity(toUser);
          txn.commit();
        }
      } finally {
          if (txn.isActive()) {
              txn.rollback();
          }
      }
      
      // If toUser has no installation, send an email invite
      if (installId == null) {
        new EmailUtil().sendUserInvite(fromEmail, toEmail);
      }
    }
    serverSocial.outbox.headId = newHeadId;
    serverSocial.outbox.tailId = clientSocial.outbox.tailId;
    serverSocial.outbox.mq.clear();
    
    // Remove inbox messages consumed at client and send the rest
    for (int i = serverSocial.inbox.mq.size() - 1; i >= 0; i--) {
      Message msg = serverSocial.inbox.mq.get(i);
      if (msg.messageId < clientSocial.inbox.headId) {
        serverSocial.inbox.mq.remove(msg);
      }
    }
    serverSocial.inbox.headId = clientSocial.inbox.headId;
    // Merge in friends list - for now copy over from client.
    serverSocial.friends = clientSocial.friends;
  }

  public void confirmRegistrationInfo(String userEmail, String installId,
      String userName, EmbeddedEntity newUserProfile, PropertyContainer newUser) {
    
    PropertyContainer oldUser = retrieveUser(installId);
    EmbeddedEntity oldUserProfile = createOrGetUserProfile(oldUser, true);
    EmbeddedEntity userProfile = newUserProfile;
    if (oldUserProfile != null && oldUser.getProperty(ProfileServlet.NEW_USER_ID) == null) {
      userProfile = oldUserProfile;
    }
    ServerProps serverProps = jsonEntityUtil.getFromJsonTextProperty(userProfile, ProfileData.SERVER_PROPS, ServerProps.class);
    serverProps.userName = userName;
    serverProps.userId = userEmail;
    serverProps.isRegistered = true;
    Date date = new Date();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    serverProps.registrationDate = dateFormat.format(date);
    jsonEntityUtil.setAsJsonTextProperty(userProfile, ProfileData.SERVER_PROPS, serverProps);
    // update timestamp of server properties which have now been updated
    EmbeddedEntity lastUpdated = (EmbeddedEntity) userProfile.getProperty(ProfileData.LAST_UPDATED);
    lastUpdated.setProperty(ProfileData.SERVER_PROPS, System.currentTimeMillis());
    
    if ((oldUser instanceof Entity) && userProfile == oldUserProfile) {
      oldUserProfile.setProperty(ProfileServlet.PROFILE, newUserProfile);      
      newUser.setPropertiesFrom(oldUser);
      newUser.setProperty(ProfileServlet.OLD_USER_ID, installId);
      oldUser.setProperty(ProfileServlet.NEW_USER_ID, userEmail);
      saveEntity(oldUser);
    }
    saveEntity(newUser);
  }

  public void saveOptionalRegistrationInfo(PropertyContainer user, EmbeddedEntity profile,
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
    
    saveEntity(user);
  }

  public void saveEntity(PropertyContainer user) {
    ds.put((Entity)user);
  }

  public void resetUserProfile(String userEmail) throws IllegalStateException {
    PropertyContainer user = retrieveUser(userEmail);
    EmbeddedEntity userProfile = retrieveUserProfile(userEmail);
    if (userProfile != null) {
      userProfile.setProperty(ProfileData.TOPIC_STATS, new EmbeddedEntity());
      EmbeddedEntity lastUpdated = (EmbeddedEntity) userProfile.getProperty(ProfileData.LAST_UPDATED);
      saveEntity(user);      
    }
  }

  public PropertyContainer createOrGetInstall(String installId, boolean create) {
    if (installId == null || installId.length() == 0) return null;
    Key key = KeyFactory.createKey(InstallProfileServlet.INSTALL_PROFILE, installId);
    Entity install = null;
    try {
      install = ds.get(key);
    } catch (EntityNotFoundException e) {
      if (create && install == null) {
        install = new Entity(InstallProfileServlet.INSTALL_PROFILE, installId);
        InstallData data = new InstallData();
        data.lastUpdated = System.currentTimeMillis();
        data.installId = installId;
        install.setProperty(InstallData.INSTALL_DATA, new Text(new Gson().toJson(data)));
        ds.put(install);
      }
    }
    return install;
  }
  
  public InstallData retrieveInstallProfile(String installId) {
    if (installId == null || installId.length() == 0) return null;
    PropertyContainer install = createOrGetInstall(installId, false);
    if (install != null) {
      return jsonEntityUtil.getFromJsonTextProperty(install, InstallData.INSTALL_DATA, InstallData.class);
    }
    return null;
  }
}
