<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.EmbeddedEntity" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.EntityNotFoundException" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="com.google.appengine.api.datastore.Text" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.reflect.TypeToken" %>

<%@ page import="java.lang.reflect.Type" %>
<%@ page import="java.util.Map" %>

<%@ page import="com.mazalearn.gwt.server.Activity" %>
<%@ page import="com.mazalearn.gwt.server.Activity.Tutor" %>
<%@ page import="com.mazalearn.gwt.server.Domain" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <head>
    <!--   <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />-->
  </head>

  <body>

<%
    String userEmail = request.getParameter("userEmail");
    if (userEmail == null) {
        userEmail = "default";
    }
    pageContext.setAttribute("userEmail", userEmail);
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
%>

<%
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(User.class.getSimpleName(), userEmail);
    Entity userEntity;
    EmbeddedEntity profile;
    try {
      userEntity = datastore.get(key);
      profile = (EmbeddedEntity) userEntity.getProperty("Profile");
    } catch (EntityNotFoundException e) {
      throw new RuntimeException("Could not find user: " + userEmail);
    }
%>

      <table>
        <tr><td>User</td><td>${fn:escapeXml(userEmail)}</td></tr>
      </table>
   
<%
     /* Get domains of this user's embedded profile entity */
   for (Domain domain: Domain.values()) {
     String domainStatsStr = ((Text) profile.getProperty(domain.name())).getValue();
     Type statsType = new TypeToken<Map<String, Float>>() {}.getType();
     Map<String, Float> stats = new Gson().fromJson(domainStatsStr, statsType);
     Activity activity1 = Activity.load(getServletContext(), "/assets/data/" + domain.name() + "/1.json");
     activity1.populateStats(stats);
   %>
    <p>
    <%= domain.name() %>
    <table border="1">
     <tr>
        <td>Goal</td>
        <td>Time Spent</td>
        <td>% Complete</td>
     </tr>
<%
     for (Tutor tutor: activity1.getTutors()) {
%>
       <tr>
         <td><%= tutor.goal %></td>
         <td><%= tutor.timeSpent %></td>
         <td><%= tutor.successPercent %></td>
       </tr>
<%       
     }
%>
     </table>
<%
     }
%>      
 

  </body>
</html>