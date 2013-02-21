<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.EntityNotFoundException" %>

<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
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
    try {
      userEntity = datastore.get(key);
    } catch (EntityNotFoundException e) {
      throw new RuntimeException("Could not find user: " + userEmail);
    }
%>

      <table>
        <tr><td>Email</td><td>${fn:escapeXml(userEmail)}</td></tr>
        <tr><td>Name</td><td><input name="username"></td></tr>
        <tr><td>Sex</td><td><input type="radio" name="sex" value="F">Female
                            <input type="radio" name="sex" value="M">Male
                            </td></tr>
        <tr><td>Grade</td><td><select name="grade">
                                       <option value=7>7</option>
                                       <option value=8>8</option>
                                       <option value=9>9</option>
                                       <option value="10">10</option>
                                       <option value="other">Other</option>
                                     </select></td></tr>
        <tr><td>School</td><td><input name="school"></td></tr>
        <tr><td>City</td><td><input name="city"></td></tr>
	      <tr><td>Comments</td><td><textarea name="comments" rows="3" cols="60"></textarea></td></tr>
      </table>
      <table>
<%  /* Get all properties of this user's embedded profile entity */
%>      
      </table>

  </body>
</html>