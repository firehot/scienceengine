package com.mazalearn.gwt.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.utils.Crypter;

/**
 * Test for Registration of users
 * 
 */

public class RegistrationFunctionalTest {

  private static final String USER_EMAIL = "user@email";
  private static final String INSTALL_ID = "installid";
  private EmbeddedEntity serverProfile = new EmbeddedEntity();
  private ProfileData clientProfile = new ProfileData();
  private RegistrationServlet registrationServlet = new RegistrationServlet();
  private ProfileServlet profileServlet = new ProfileServlet();
  private RegistrationEmailServlet registrationEmailServlet = new RegistrationEmailServlet();
  private DummyHttpServletRequest request;
  private DummyHttpServletResponse response;
  private StringWriter out;
  private MockProfileUtil mockProfileUtil = new MockProfileUtil();
  private JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();
  private MockEmailUtil mockEmailUtil;
  private PropertyContainer installEntity;

  @BeforeClass
  public static void staticSetUp() {
  }
  
  @AfterClass
  public static void staticTearDown() {
  }

  @Before
  public void setUp() {
    serverProfile.setProperty(ProfileData.CLIENT_PROPS, new Text("{clientjson:test}"));
    serverProfile.setProperty(ProfileData.SERVER_PROPS, new Text("{serverjson:test}"));
    
    clientProfile.client = new ClientProps();
    clientProfile.server = new ServerProps();
    
    request = new DummyHttpServletRequest();
    response = new DummyHttpServletResponse();
    out = new StringWriter();
    response.setWriter(new PrintWriter(out, true));
    profileServlet.setProfileUtil(mockProfileUtil);
    registrationServlet.setProfileUtil(mockProfileUtil);
    registrationEmailServlet.setProfileUtil(mockProfileUtil);
    mockEmailUtil = new MockEmailUtil();
    registrationServlet.setEmailUtil(mockEmailUtil);
    installEntity = new EmbeddedEntity();

  }

  @Test
  public void testRegistrationFlow_Normal() throws IOException, ServletException {
    /* First installation occurs at client - installid is created */
    String userId = installAtClient();
   
    /* Sync profile to server */   
    PropertyContainer user = syncProfileToServer(userId);
    
    /* User invokes registration at server - useremail is associated with installid in email */    
    registerAtServer(user, INSTALL_ID, installEntity);
    
    /* Email is clicked at client - useremail is linked to installid */
    confirmEmailUrl();
    
    // useremail is synced back to client
    String syncStr = mockProfileUtil.saveUserProfile(INSTALL_ID, clientProfile);
  }

  @Test
  public void testRegistrationFlow_ClientLosesProfile() throws IOException, ServletException {
    /* First installation occurs at client - installid is created */
    String userId = installAtClient();
   
    /* Sync profile to server */   
    PropertyContainer user = syncProfileToServer(userId);
    
    /* User invokes registration at server - useremail is associated with installid in email */    
    registerAtServer(user, INSTALL_ID, installEntity);
    
    /* Email is clicked at client - useremail is linked to installid */
    confirmEmailUrl();
    
    /* User has registered and lost profile - but installid is the same.
     * Client sync is based on installid and does not get back profile.
     * Again invokes registration at server - useremail should be associated with installid in email */    
    registerAtServer(user, INSTALL_ID, installEntity);
    
    /* Email is clicked at client - useremail is linked to installid */
    confirmEmailUrl();    
  }

  @Test
  public void testRegistrationFlow_ClientReinstalls() throws IOException, ServletException {
    /* First installation occurs at client - installid is created */
    String userId = installAtClient();
   
    /* Sync profile to server */   
    PropertyContainer user = syncProfileToServer(userId);
    
    /* User invokes registration at server - useremail is associated with installid in email */    
    registerAtServer(user, INSTALL_ID, installEntity);
    
    /* Email is clicked at client - useremail is linked to installid */
    confirmEmailUrl();
    
    /* User has lost profile - 
     * Again invokes registration at server - useremail should be associated with installid in email */    
    registerAtServer(user, "installid2", new EmbeddedEntity());
    
    /* Email is clicked at client - useremail is linked to installid */
    confirmEmailUrl();    
  }

  private void confirmEmailUrl() throws IOException, ServletException {
    // setup server side data
    EmbeddedEntity installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);
    
    // set up client request url
    long timeNow = System.currentTimeMillis();
    String hash = Crypter.saltedSha1Hash(INSTALL_ID + USER_EMAIL + "username" + timeNow, 
        INSTALL_ID);
    request.setParameters("e",USER_EMAIL, "i", INSTALL_ID,
        "n", "username", "t", String.valueOf(timeNow), "h", hash);
    
    // set up server side response for testing
    out = new StringWriter();
    response.setWriter(new PrintWriter(out, true));
    
    // do servlet request
    registrationEmailServlet.doGet(request, response);
    
    assertEquals("<div style='background-color: black; width:64'><img src='/userimage?userid=" +
        USER_EMAIL +
        "&png=pnguser'></div>Installation not yet registered - registering to user: " +
        USER_EMAIL +
        "<br>Registration Completed: Thank you, " +
        USER_EMAIL, out.toString());
  }

  private void registerAtServer(PropertyContainer user, String installId,
      PropertyContainer installEntity) throws IOException, ServletException {
    // setup server environment
    mockProfileUtil.setUserEntity("user@email", user);
    mockProfileUtil.setInstallEntity(installEntity);
    
    // setup request
    request.setParameters(ProfileData.USER_EMAIL, "user@email",
        ProfileData.USER_NAME, "om", 
        ProfileData.INSTALL_ID, installId);
    
    // set up server side response for testing
    out = new StringWriter();
    response.setWriter(new PrintWriter(out, true));
    
    // invoke registration
    registrationServlet.doPost(request, response);
    
    // Now registration email should associate user email with installid
    assertEquals("<html><body><br><br>Registration in progress: " + "user@email" +
        "<br><br>Email has been sent. <br>Please click on URL in email to complete registration." +
        "</body></html>", out.toString());   
    assertEquals(USER_EMAIL, mockEmailUtil.getMailSentTo());
  }

  private PropertyContainer syncProfileToServer(String userId)
      throws IOException, ServletException {
    // set up client request
    String json = new Gson().toJson(clientProfile);
    String profileStr = Base64.encode(json);
    request.setInputStream(profileStr);
    
    // set up server environment to create a new user
    PropertyContainer user = new EmbeddedEntity();
    mockProfileUtil.setUserEntity("user@email", user);
    
    // invoke profile servlet
    profileServlet.doPost(request, response);
    
    // synced to server - server should now have installid
    user = mockProfileUtil.getSavedEntity();
    PropertyContainer serverProfile = (PropertyContainer) user.getProperty(ProfileServlet.PROFILE);
    ClientProps clientProps = new JsonEntityUtil().getFromJsonTextProperty(serverProfile, ProfileData.CLIENT_PROPS, ClientProps.class);
    assertEquals(userId, clientProps.installId);
    return user;
  }

  private String installAtClient() {
    String userId = INSTALL_ID;
    clientProfile.client.installId = userId;
    clientProfile.lastUpdated.put(ProfileData.CLIENT_PROPS, 100L);
    return userId;
  }
  
}
