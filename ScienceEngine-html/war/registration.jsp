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

<%@ page import="com.mazalearn.scienceengine.app.services.ProfileData" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <head>
    <!--   <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />-->
  </head>

  <body>

<%
    String installId = request.getParameter(ProfileData.INSTALL_ID);
    if (installId == null) {
        installId = "Unknown Installation";
    }
    String userEmail = request.getParameter(ProfileData.USER_EMAIL);
    String userId = (userEmail.length() > 0) ? userEmail : installId;

%>

    <form action="/register" method="post">
      <input type="hidden" name="<%= ProfileData.INSTALL_ID %>" value=<%= request.getParameter(ProfileData.INSTALL_ID) %>>
      <div style="background-color: black; width:64">
        <img src='/userimage?userid=<%= userId %>&png=pnguser'>
      </div>
      <table>
        <tr><td>Email*</td><td><input name="<%= ProfileData.USER_EMAIL %>" value="<%= userEmail %>"></td></tr>
        <tr><td>Name*</td><td><input name="<%= ProfileData.USER_NAME %>"></td></tr>
        <tr><td>Sex</td><td><input type="radio" name="<%= ProfileData.SEX %>" value="F">Female
                            <input type="radio" name="<%= ProfileData.SEX %>" value="M">Male
                            </td></tr>
        <tr><td>Grade</td><td><select name="<%= ProfileData.GRADE %>">
                                       <option value=7>7</option>
                                       <option value=8>8</option>
                                       <option value=9>9</option>
                                       <option value="10">10</option>
                                       <option value="other">Other</option>
                                     </select></td></tr>
        <tr><td>School</td><td><input name="<%= ProfileData.SCHOOL %>"></td></tr>
        <tr><td>City</td><td><input name="<%= ProfileData.CITY %>"></td></tr>
	      <tr><td>Comments</td><td><textarea name="<%= ProfileData.COMMENTS %>" rows="3" cols="60"></textarea></td></tr>
      </table>
      <div><input type="submit" value="Register" /></div>
    </form>

  </body>
</html>