package com.mazalearn.gwt.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.services.ProfileData.Social;
import com.mazalearn.scienceengine.app.services.ProfileData.Social.Message;

@SuppressWarnings("serial")
public class ProfileServlet extends HttpServlet {

  public static final String PROFILE = "Profile";
  // The profileId in a user entity forwards to the right profile.
  public static final String NEW_USER_ID = "newuserid"; // email verification is the owner
  public static final String OLD_USER_ID = "olduserid"; // email verification is the owner

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    System.out.println("Received post: " + request.getContentLength());
    String userId = request.getHeader(ProfileData.USER_ID);
    System.out.println("UserId: " + userId);
    BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
    byte[] profileBytes = new byte[request.getContentLength()];
    bis.read(profileBytes);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    String oldUserId = saveUserProfile(userId, profileBytes, ds);
    bis.close();
    writeProfileResponse(response, userId, ds);
    // Delete old user, if any
    if (oldUserId != null) {
      Key key = KeyFactory.createKey(User.class.getSimpleName(), oldUserId.toLowerCase());
      ds.delete(key);
      System.out.println("Deleted: " + oldUserId);
      Entity newUser = retrieveUser(userId, ds);
      newUser.removeProperty(OLD_USER_ID);
      ds.put(newUser);
    }
  }
  
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String userId = request.getParameter(ProfileData.USER_ID);
    System.out.println("Received get: " + userId);
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    writeProfileResponse(response, userId, ds);
  }

  private void writeProfileResponse(HttpServletResponse response, String userId, DatastoreService ds)
      throws IOException {
    String responseStr = getUserProfileAsBase64(userId, ds);
    if (responseStr.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    } else {
      response.getWriter().append(responseStr);
    }
    response.getWriter().close();
  }
  
  private String saveUserProfile(String userId, byte[] profileBytes, DatastoreService ds) 
      throws IllegalStateException {
    Entity user = createOrGetUser(userId, ds, true);
    String clientProfileBase64 = new String(profileBytes);
    String clientProfileJson = new String(Base64.decode(clientProfileBase64));
    
    // Trim at end where 0 chars are present.
    int count = clientProfileJson.length();
    while (clientProfileJson.charAt(--count) == 0);
    Gson gson = new Gson();
    System.out.println("saveUserProfile:" + clientProfileJson.substring(0, count + 1));
    // Get profile data and sync with existing one
    ProfileData clientProfile = gson.fromJson(clientProfileJson.substring(0, count+1), ProfileData.class);
    
    EmbeddedEntity serverProfileEntity = createOrGetUserProfile(user, true);
    
    // Client Properties
    Text storedClientPropertiesJson = (Text) serverProfileEntity.getProperty(ProfileData.CLIENT_PROPS);
    ClientProps storedClientProperties = storedClientPropertiesJson != null ? gson.fromJson(storedClientPropertiesJson.getValue(),  ProfileData.ClientProps.class) : new ClientProps();
    ClientProps clientProperties = clientProfile.client;
    if (clientProperties.lastUpdated > storedClientProperties.lastUpdated) {
      serverProfileEntity.setProperty(ProfileData.CLIENT_PROPS, new Text(gson.toJson(clientProperties)));
    } else {
      serverProfileEntity.setProperty(ProfileData.CLIENT_PROPS, new Text(gson.toJson(storedClientProperties)));
    }
    
    // TopicStats
    EmbeddedEntity topicStatsEntity = (EmbeddedEntity) serverProfileEntity.getProperty(ProfileData.TOPIC_STATS);
    if (topicStatsEntity == null) {
      topicStatsEntity = new EmbeddedEntity();
    }
    for (Map.Entry<String, Map<String, float[]>> topicStats: clientProfile.topicStats.entrySet()) {
      String topicStatsJson = gson.toJson(topicStats.getValue());
      topicStatsEntity.setProperty(topicStats.getKey(), new Text(topicStatsJson));
    }
    serverProfileEntity.setProperty(ProfileData.TOPIC_STATS, topicStatsEntity);
    
    // Social
    // extract server profile social
    Text serverSocialJson = (Text) serverProfileEntity.getProperty(ProfileData.SOCIAL);
    Social serverSocial = serverSocialJson != null ? gson.fromJson(serverSocialJson.getValue(), ProfileData.Social.class) : new Social();
    Social clientSocial = clientProfile.social;
    // Clear out all outbox messages from client
    for (Message msg: clientSocial.outbox) {
      // If message has already been processed, ignore it.
      if (msg.messageId < serverSocial.lastOutboxMessageId) continue;
      String toEmail = msg.email;
      Entity toUser = createOrGetUser(toEmail, ds, true);
      EmbeddedEntity toUserProfile = createOrGetUserProfile(toUser, true);
      // If toUser has no installation, send an email invite
      if (toUserProfile.getProperty(ProfileData.INSTALL_ID) == null) {
        sendUserInvite(userId, toEmail);
      } 
      // add gift to inbox of toUser's social
      // TODO: We should ideally lock toUser but we are being careless here
      Text toSocialJson = (Text) toUserProfile.getProperty(ProfileData.SOCIAL);
      Social toSocial = toSocialJson != null 
          ? gson.fromJson(toSocialJson.getValue(), ProfileData.Social.class)
          : new Social();
      System.out.println("Transferring Gift " + msg.messageId + " to " + toEmail);
      msg.messageId = toSocial.lastInboxMessageId++;
      msg.email = userId;
      toSocial.inbox.add(msg);
      toUserProfile.setProperty(ProfileData.SOCIAL, new Text(gson.toJson(toSocial)));
      ds.put(toUser);
    }
    serverSocial.lastOutboxMessageId = clientSocial.lastOutboxMessageId;
    // Merge in friends list - for now copy over from client.
    serverSocial.friends = clientSocial.friends;
    
    serverProfileEntity.setProperty(ProfileData.SOCIAL, new Text(gson.toJson(serverSocial)));
    ds.put(user);
    // If retrieved user is for requested userid and not forwarded
    if (userId.equals(user.getKey().getName())) {
      return (String) user.getProperty(OLD_USER_ID);
    }
    return null;
  }
  
  private void sendUserInvite(String fromEmail, String toEmail) {
    Properties properties = new Properties();
    Session session = Session.getDefaultInstance(properties, null);

    String msgBody = 
    		"Your friend " + toEmail + " has sent you a Science gift." +
        "\nTo use the gift, you have to install Science Engine." +
        "\n\n-MazaLearn";

    try {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("admin@mazalearn.com", "Mazalearn Admin"));
        msg.addRecipient(MimeMessage.RecipientType.TO,
                         new InternetAddress(toEmail, "User"));
        msg.setSubject("Collect Science Engine Gift sent by " + toEmail);
        msg.setText(msgBody);
        Transport.send(msg);

    } catch (AddressException e) {
        // ...
    } catch (MessagingException e) {
        // ...
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }    
  }


  public static EmbeddedEntity createOrGetUserProfile(Entity user, boolean create) {
    if (user == null) return null;
    EmbeddedEntity profileEntity = (EmbeddedEntity) user.getProperty(PROFILE);
    if (create && profileEntity == null) {
      System.out.println("Creating profile for user:" + user.getKey().getName());
      profileEntity = new EmbeddedEntity();
      user.setProperty(PROFILE, profileEntity);
    }
    return profileEntity;
  }

  public static Entity createOrGetUser(String userId, DatastoreService ds, boolean create) {
    if (userId == null || userId.length() == 0) return null;
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userId.toLowerCase());
    Entity user = null, user1 = null;
    try {
      user = ds.get(key);
      String profileId = (String) user.getProperty(NEW_USER_ID);
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
  
  public String getUserProfileAsBase64(String userId, DatastoreService ds) 
      throws IllegalStateException {
    EmbeddedEntity profileEntity = retrieveUserProfile(userId, ds);
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
      }
    }
    
    EmbeddedEntity topicStatsEntity = (EmbeddedEntity) profileEntity.getProperty(ProfileData.TOPIC_STATS);
    topicDelimiter = ", topicStats: { ";
    for (Map.Entry<String, Object> property: topicStatsEntity.getProperties().entrySet()) {
      Object value = property.getValue();
      if (value instanceof Text) {
        String v = (value == null) ? "{}" : ((Text) value).getValue();
        if (!"null".equals(v)) {
          json.append(topicDelimiter + property.getKey() + ":" + v);
          topicDelimiter = ",";
        }
      }
    }
    json.append("}}");
    System.out.println("getUserProfileAsBase64: " + json);
    String profileStringBase64 = Base64.encode(json.toString());
    return profileStringBase64;
  }

  public static EmbeddedEntity retrieveUserProfile(String userId, DatastoreService ds) {
    Entity user = createOrGetUser(userId, ds, false);
    return createOrGetUserProfile(user, false);
  }

  public static Entity retrieveUser(String userId, DatastoreService ds) {
    return createOrGetUser(userId, ds, false);
  }
}
