package com.mazalearn.gwt.server;

import static org.junit.Assert.*;
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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.utils.Crypter;

/**
 * Test for RegistrationEmailServlet
 * 
 */

public class RegistrationEmailServletTest {

  private static final String INSTALL_ID = "installid";
  private static final String USER_EMAIL = "user@email";
  private EmbeddedEntity serverProfile = new EmbeddedEntity();
  private ProfileData clientProfile = new ProfileData();
  private RegistrationEmailServlet registrationEmailServlet = new RegistrationEmailServlet();
  private DummyHttpServletRequest request;
  private DummyHttpServletResponse response;
  private StringWriter out;
  private MockProfileUtil mockProfileUtil;

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
    setUpMessage(System.currentTimeMillis());     
    response = new DummyHttpServletResponse();
    out = new StringWriter();
    response.setWriter(new PrintWriter(out, true));
    mockProfileUtil = new MockProfileUtil();
    registrationEmailServlet.setProfileUtil(mockProfileUtil);
  }

  private void setUpMessage(long timeNow) {
    String hash = Crypter.saltedSha1Hash(INSTALL_ID + USER_EMAIL + "username" + timeNow, 
        INSTALL_ID);
    request.setParameters("e",USER_EMAIL, "i", INSTALL_ID,
        "n", "username", "t", String.valueOf(timeNow), "h", hash);
  }
  
  @Test
  public void testDoGet_Expired() throws IOException, ServletException {
    long timeNow = System.currentTimeMillis() - RegistrationServlet.EXPIRY_TIME_MS;
    setUpMessage(timeNow);
    registrationEmailServlet.doGet(request, response);
    assertEquals("Registration email has expired", out.toString());
  }

  @Test
  public void testDoGet_InvalidHash() throws IOException, ServletException {
    request.setParameters("h", "invalidhash");
    registrationEmailServlet.doGet(request, response);
    assertEquals("Invalid registration info for: " + USER_EMAIL + "<br>", out.toString());
  }

  @Test
  public void testDoGet_RegisterInstallation_NotFound() throws IOException, ServletException {    
    mockProfileUtil.setInstallEntity(null);

    try {
      registrationEmailServlet.doGet(request, response);
      fail();
    } catch (UnsupportedOperationException e) {
      assertEquals("Installation not found - sync not done?", e.getMessage());
    }
  }

  @Test
  public void testDoGet_RegisterInstallation_AlreadyRegistered() throws IOException, ServletException {
    JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();
    
    EmbeddedEntity installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    installData.registeredUserId = "test@test.com";
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);

    EmbeddedEntity user = new EmbeddedEntity();
    EmbeddedEntity userProfile = ProfileUtil.createOrGetUserProfile(user, true);
    ServerProps serverProps = new ServerProps();
    serverProps.isRegistered = true;
    jsonEntityUtil.setAsJsonTextProperty(userProfile, ProfileData.SERVER_PROPS, serverProps);
    mockProfileUtil.setUserEntity(USER_EMAIL, user);
    
    registrationEmailServlet.doGet(request, response);
    
    assertEquals("Installation already registered to: " +
    		USER_EMAIL +
        "\nOnly one user can be registered for an installation", out.toString());
  }

  @Test
  public void testDoGet_RegisterInstallation_UserIsInstaller() throws IOException, ServletException {
    JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();
    
    EmbeddedEntity installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);

    EmbeddedEntity user = new EmbeddedEntity();
    mockProfileUtil.setUserEntity(USER_EMAIL, user);
    
    registrationEmailServlet.doGet(request, response);
    
    assertEquals("<div style='background-color: black; width:64'><img src='/userimage?userid=" +
        USER_EMAIL + "&png=pnguser'></div>Installation not yet registered - registering to user: " +
        USER_EMAIL +
        "<br>Registration Completed: Thank you, " +
        USER_EMAIL, out.toString());
    EmbeddedEntity profile = (EmbeddedEntity) user.getProperty(ProfileServlet.PROFILE);
    ServerProps serverProps = jsonEntityUtil.getFromJsonTextProperty(profile, ProfileData.SERVER_PROPS, ServerProps.class);
    assertEquals(USER_EMAIL, serverProps.userId);
    assertTrue(serverProps.isRegistered);
    
    // Try registering again
    out = new StringWriter();
    response.setWriter(new PrintWriter(out, true));
    registrationEmailServlet.doGet(request, response);
    
    assertEquals("<div style='background-color: black; width:64'><img src='/userimage?userid=" +
        USER_EMAIL + "&png=pnguser'></div>This installation is registered to: " +
        USER_EMAIL +
        "<br>Registration Completed: Thank you, " +
        USER_EMAIL, out.toString());
  }

  @Test
  public void testDoGet_RegisterInstallation_UserIsNotInstaller() throws IOException, ServletException {
    JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();
    
    EmbeddedEntity installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    installData.registeredUserId = "test@test.com";
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);

    EmbeddedEntity user = new EmbeddedEntity();
    mockProfileUtil.setUserEntity(USER_EMAIL, user);
    
    registrationEmailServlet.doGet(request, response);
    
    assertEquals("Installation already registered to: " +
        USER_EMAIL +
        "\nOnly one user can be registered for an installation", out.toString());
  }
    
  @Test
  public void testDoGet_RegisterInstallation_UserIsNotInstaller_Enterprise() throws IOException, ServletException {
    JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();
    
    EmbeddedEntity installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    installData.registeredUserId = "test@test.com";
    installData.enterpriseId = "mazalearn";
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);

    EmbeddedEntity user = new EmbeddedEntity();
    mockProfileUtil.setUserEntity(USER_EMAIL, user);
    
     registrationEmailServlet.doGet(request, response);
    
    assertEquals("<div style='background-color: black; width:64'><img src='/userimage?userid=" +
    		USER_EMAIL +
    		"&png=pnguser'></div>This installation is registered to: " +
    		"test@test.com" +
    		"<br>Registration Completed: Thank you, " +
    		USER_EMAIL, out.toString());
    EmbeddedEntity profile = (EmbeddedEntity) user.getProperty(ProfileServlet.PROFILE);
    ServerProps serverProps = jsonEntityUtil.getFromJsonTextProperty(profile, ProfileData.SERVER_PROPS, ServerProps.class);
    assertEquals(USER_EMAIL, serverProps.userId);
    assertTrue(serverProps.isRegistered);

    // Try registering again
    out = new StringWriter();
    response.setWriter(new PrintWriter(out, true));
    registrationEmailServlet.doGet(request, response);
    
    assertEquals("<div style='background-color: black; width:64'><img src='/userimage?userid=" +
        USER_EMAIL +
        "&png=pnguser'></div>This installation is registered to: " +
        "test@test.com" +
        "<br>Registration Completed: Thank you, " +
        USER_EMAIL, out.toString());
  }

}
