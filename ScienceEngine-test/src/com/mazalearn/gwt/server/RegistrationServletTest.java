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
import com.mazalearn.scienceengine.app.services.InstallData;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;

/**
 * Test for RegistrationEmailServlet
 * 
 */

public class RegistrationServletTest {

  private static final String USER_EMAIL = "user@email";
  private static final String INSTALL_ID = "installid";
  private EmbeddedEntity serverProfile = new EmbeddedEntity();
  private ProfileData clientProfile = new ProfileData();
  private RegistrationServlet registrationServlet = new RegistrationServlet();
  private DummyHttpServletRequest request;
  private DummyHttpServletResponse response;
  private StringWriter out;
  private JsonEntityUtil jsonEntityUtil = new JsonEntityUtil();
  private MockProfileUtil mockProfileUtil = new MockProfileUtil();
  private MockEmailUtil mockEmailUtil = new MockEmailUtil();

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
    registrationServlet.setProfileUtil(mockProfileUtil);
    registrationServlet.setEmailUtil(mockEmailUtil);
  }

  @Test
  public void testDoPost_BadEmail1() throws IOException, ServletException {
    registrationServlet.doPost(request, response);
    assertEquals("Improper email. Cannot register", out.toString());
  }
  
  @Test
  public void testDoPost_BadEmail2() throws IOException, ServletException {
    request.setParameters(ProfileData.USER_EMAIL, "useremail");
    registrationServlet.doPost(request, response);
    assertEquals("Improper email. Cannot register", out.toString());
  }

  @Test
  public void testDoPost_BadName1() throws IOException, ServletException {
    request.setParameters(ProfileData.USER_EMAIL, "useremail");
    registrationServlet.doPost(request, response);
    assertEquals("Improper email. Cannot register", out.toString());
  }

  @Test
  public void testDoPost_BadName2() throws IOException, ServletException {
    request.setParameters(ProfileData.USER_EMAIL, "useremail",
        ProfileData.USER_NAME, "o");
    registrationServlet.doPost(request, response);
    assertEquals("Improper email. Cannot register", out.toString());
  }

  @Test
  public void testDoPost_RegisterInstallation_InstallNotFound() throws IOException, ServletException {
    // Set up user email and user name
    request.setParameters(ProfileData.INSTALL_ID, INSTALL_ID,
        ProfileData.USER_EMAIL, USER_EMAIL,
        ProfileData.USER_NAME, "om");
    // Setup "No install" for this install id
    mockProfileUtil.setInstallEntity(null);
    
    registrationServlet.doPost(request, response);
    assertEquals("Not properly synced to server? Could not find installation", out.toString());
  }

  @Test
  public void testDoPost_RegisterInstallation_InstallRegisteredToDiffUser() throws IOException, ServletException {
    // Set up user email and user name
    request.setParameters(ProfileData.INSTALL_ID, INSTALL_ID,
        ProfileData.USER_EMAIL, USER_EMAIL,
        ProfileData.USER_NAME, "om");
    // Setup "registered install" for this install id
    PropertyContainer installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    installData.registeredUserId = "diffuser";
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);
    
    registrationServlet.doPost(request, response);
    assertEquals("Installation already registered to: user@email\n" +
                 "Only one user can be registered for an installation", out.toString());
  }

  @Test
  public void testDoPost_RegisterInstallation_InstallRegisteredToSameUser1() throws IOException, ServletException {
    // Set up user email and user name
    request.setParameters(ProfileData.INSTALL_ID, INSTALL_ID,
        ProfileData.USER_EMAIL, USER_EMAIL,
        ProfileData.USER_NAME, "om");
    // Setup "registered install" for this install id
    PropertyContainer installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    installData.registeredUserId = USER_EMAIL;
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);
    // Setup "unregistered User" for this email for same install id
    EmbeddedEntity user = new EmbeddedEntity();
    mockProfileUtil.setUserEntity(INSTALL_ID, user);
    
    registrationServlet.doPost(request, response);
    assertEquals("<html><body><br><br>Registration in progress: " +
        USER_EMAIL +
        "<br><br>Email has been sent. <br>" +
        "Please click on URL in email to complete registration.</body></html>", out.toString());
  }

  @Test
  public void testDoPost_RegisterInstallation_InstallRegisteredToSameUser2() throws IOException, ServletException {
    // Set up request parameters
    request.setParameters(ProfileData.INSTALL_ID, INSTALL_ID,
        ProfileData.USER_EMAIL, USER_EMAIL,
        ProfileData.USER_NAME, "om");

    // Setup "registered install" for this install id
    PropertyContainer installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    installData.registeredUserId = USER_EMAIL;
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);
    
    // Setup "unregistered User" for this email for same install id
    EmbeddedEntity user = new EmbeddedEntity();
    mockProfileUtil.setUserEntity(USER_EMAIL, user);
    
    registrationServlet.doPost(request, response);
    assertEquals("<html><body><br><br>Registration in progress: " +
        USER_EMAIL +
        "<br><br>Email has been sent. <br>" +
        "Please click on URL in email to complete registration.</body></html>", out.toString());
  }

  @Test
  public void testDoPost_RegisterInstallation_ImproperUser() throws IOException, ServletException {
    // Set up request parameters
    request.setParameters(ProfileData.INSTALL_ID, INSTALL_ID,
        ProfileData.USER_EMAIL, USER_EMAIL,
        ProfileData.USER_NAME, "om");
    // Setup "registered install" for this install id
    PropertyContainer installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    installData.registeredUserId = USER_EMAIL;
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);
    // Setup "improper User" for this email for same install id
    EmbeddedEntity user = new EmbeddedEntity();
    mockProfileUtil.setUserEntity(INSTALL_ID, user);
    user.setProperty("KEY", "forwarded user");
    
    
    registrationServlet.doPost(request, response);
    assertEquals("Not properly synced to server? Could not find user", out.toString());
  }

  @Test
  public void testDoPost_RegisterInstallation_InstallRegisteredToDiffUser_Enterprise() throws IOException, ServletException {
    // Set up user email and user name
    request.setParameters(ProfileData.INSTALL_ID, INSTALL_ID,
        ProfileData.USER_EMAIL, USER_EMAIL,
        ProfileData.USER_NAME, "om");
    // Setup "registered install" for this install id
    PropertyContainer installEntity = new EmbeddedEntity();
    InstallData installData = new InstallData();
    installData.registeredUserId = "diffuser";
    installData.enterpriseId = "mazalearn";
    jsonEntityUtil.setAsJsonTextProperty(installEntity, InstallData.INSTALL_DATA, installData);
    mockProfileUtil.setInstallEntity(installEntity);
    // Setup "diffUser" for this email
    EmbeddedEntity user = new EmbeddedEntity();
    mockProfileUtil.setUserEntity(INSTALL_ID, user);
    
    registrationServlet.doPost(request, response);
    assertEquals("<html><body><br><br>Registration in progress: " + USER_EMAIL +
        "<br><br>Email has been sent. <br>Please click on URL in email to complete registration." +
        "</body></html>", out.toString());
    assertEquals(user, mockProfileUtil.getSavedEntity());
    assertEquals(USER_EMAIL, mockEmailUtil.getMailSentTo());
  }
}
