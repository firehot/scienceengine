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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.utils.Crypter;

/**
 * Test for RegistrationEmailServlet
 * 
 */

public class RegistrationEmailServletTest {

  private EmbeddedEntity serverProfile = new EmbeddedEntity();
  private ProfileData clientProfile = new ProfileData();
  private RegistrationEmailServlet registrationEmailServlet = new RegistrationEmailServlet();
  private DummyHttpServletRequest request;
  private DummyHttpServletResponse response;
  private StringWriter out;

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
  }

  private void setUpMessage(long timeNow) {
    String hash = Crypter.saltedSha1Hash("installid" + "useremail" + "username" + timeNow, 
        "installid");
    request.setParameters("e","useremail", "i", "installid",
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
    assertEquals("Invalid registration info for: useremail", out.toString());
  }

  @Test
  public void testDoGet_RegisterInstallation() throws IOException, ServletException {
    DummyProfileUtil dummyProfileUtil = new DummyProfileUtil();
    EmbeddedEntity installEntity = new EmbeddedEntity();

 //   dummyProfileUtil.setInstallEntity(installEntity);
    registrationEmailServlet.setProfileUtil(dummyProfileUtil);
    registrationEmailServlet.doGet(request, response);
  }

}
