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
<%@ page import="com.mazalearn.gwt.server.Topic" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <head>
    <!--   <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />-->
  <head>
    <!--Load the AJAX API-->
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      var timeRows;
      
      // Load the Visualization API and the piechart package.
      google.load('visualization', '1.0', {'packages':['corechart']});

      // Set a callback to run when the Google Visualization API is loaded.
      google.setOnLoadCallback(drawChart);

      // Callback that creates and populates a data table,
      // instantiates the pie chart, passes in the data and
      // draws it.
      function drawChart() {

        // Create the data table.
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Learning Goal');
        data.addColumn('number', 'Time');
        data.addRows(timeRows);

        // Set chart options
        var options = {'title':'Progress: Time Spent',
                       'width':400,
                       'height':300};

        // Instantiate and draw the chart, passing in some options.
        var chart = new google.visualization.PieChart(document.getElementById('chart_div'));
        chart.draw(data, options);
      }
    </script>
  </head>
  </head>

  <body>
    <div id="chart_div"></div>

<%
  String userEmail = request.getParameter("userEmail");
    if (userEmail == null) {
      userEmail = "DemoUser@mazalearn.com";
    }
    Topic topic = Topic.Electromagnetism;
    try {
      topic = Topic.valueOf(request.getParameter("topic"));
    } catch(Exception ignored) {};
    pageContext.setAttribute("userEmail", userEmail);
    pageContext.setAttribute("topic", topic);
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
   String domainStatsStr = ((Text) profile.getProperty(topic.name())).getValue();
   Type statsType = new TypeToken<Map<String, Float>>() {}.getType();
   Map<String, Float> stats = new Gson().fromJson(domainStatsStr, statsType);
   Activity activity1 = Activity.load(getServletContext(), "/assets/data/" + topic.name() + "/1.json");
   activity1.populateStats(stats);
   %>
    <p>
    <%= topic.name() %>
    <img src="/assets/data/<%= topic.name() %>/1.png" width=400>
    <table border="1">
     <tr>
        <td>Goal</td>
        <td>Time Spent</td>
        <td>% Complete</td>
     </tr>
<%
     String json = "[";
     String delimiter = "";
     for (Tutor tutor: activity1.getTutors()) {
       json += delimiter + "['" + tutor.id + "'," + tutor.timeSpent + "]";
       delimiter = ",";
%>
       <tr>
         <td><%= tutor.goal %></td>
         <td><%= tutor.timeSpent %></td>
         <td><%=tutor.completionPercent%></td>
       </tr>
<%       
     }
     json += "]";
%>
     </table>
     <script>
       var timeRows = <%= json %>;
     </script>
     <div id="chart_div"></div>
  </body>
</html>