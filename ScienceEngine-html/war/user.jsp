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
    if (user != null) {
      pageContext.setAttribute("user", user);
%>
<p>Hello, ${fn:escapeXml(user.nickname)}! 
<%
    } else {
%>
<p>Hello!

<%
    }
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
        <tr><td>Email</td><td>${fn:escapeXml(userEmail)}</td></tr>
      </table>
   
<%
     /* Get domains of this user's embedded profile entity */
   for (Domain domain: Domain.values()) {
      String domainProgressStr = ((Text) profile.getProperty(domain.name())).getValue();
      Type statsType = new TypeToken<Map<String, Float>>() {}.getType();
      Map<String, Float> stats = new Gson().fromJson(domainProgressStr, statsType);
   %>
    <table>
<%
     for (String pkey: stats.keySet()) {
%>
       <tr><td><%= pkey %></td><td><%= stats.get(pkey) %></td></tr>
<%       
     }
%>
     </table>
<%
     }
%>      
 

  </body>
</html>