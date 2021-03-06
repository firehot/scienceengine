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
<%@ page import="com.mazalearn.gwt.server.ProfileUtil" %>
<%@ page import="com.mazalearn.scienceengine.app.services.ProfileData" %>
<%@ page import="com.mazalearn.scienceengine.Topic" %>
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
                       'height':300,
                       chartArea:{left:0,top:0}};

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
    String userId = request.getParameter(ProfileData.USER_ID);
    if (userId == null) {
      userId = "demouser@mazalearn.com";
    }

    Topic topic = Topic.Electromagnetism;
    try {
      topic = Topic.valueOf(request.getParameter("topic"));
    } catch(Exception ignored) {};
    
    Topic activityLevel = Topic.BarMagnet;
    try {
      activityLevel = Topic.valueOf(request.getParameter("activity"));
    } catch (Exception ignored) {};
    
    pageContext.setAttribute("userid", userId);
    pageContext.setAttribute("topic", topic);
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
%>

<%
    ProfileUtil profileUtil = new ProfileUtil();
    EmbeddedEntity profile = profileUtil.retrieveUserProfile(userId);
%>

      <table>
        <tr><td>User</td><td>${fn:escapeXml(userEmail)}</td></tr>
      </table>
    <p>
    <%= topic.name() %> <br/>
    <%= activityLevel.name() %>
    <img src='/assets/data/<%= topic.name() + "/" + activityLevel + ".png" %>' width=400>
   
<%
   String activityStatsStr = ((Text) profile.getProperty(activityLevel.name())).getValue();
   Type statsType = new TypeToken<Map<String, float[]>>() {}.getType();
   Map<String, float[]> stats = new Gson().fromJson(activityStatsStr, statsType);
   Activity activity = Activity.load(getServletContext(), topic, activityLevel);
   activity.populateStats(stats);
   %>
    <table border="1">
     <tr>
        <td>Goal</td>
        <td>Time Spent</td>
        <td>Num Attempts</td>
        <td>Num Successes</td>
        <td>Failure Tracker</td>
        <td>% Attempted</td>
        <td>Points</td>
     </tr>
<%
     String json = "[";
     String delimiter = "";
     for (Tutor tutor: activity.getTutors()) {
       if (tutor.type.equals("KnowledgeUnit")) {
         json += delimiter + "['" + tutor.id + "'," + tutor.stats[Activity.Tutor.TIME_SPENT] + "]";
         delimiter = ",";
       }
%>
       <tr>
         <td><%= tutor.goal %></td>
         <td><%= Math.round(tutor.stats[Activity.Tutor.TIME_SPENT]) %></td>
         <td><%= Math.round(tutor.stats[Activity.Tutor.NUM_ATTEMPTS]) %></td>
         <td><%= Math.round(tutor.stats[Activity.Tutor.NUM_SUCCESSES]) %></td>
         <td><%= Math.round(tutor.stats[Activity.Tutor.FAILURE_TRACKER]) %></td>
         <td><%= Math.round(tutor.stats[Activity.Tutor.PERCENT_PROGRESS]) %></td>
         <td><%= Math.round(tutor.stats[Activity.Tutor.POINTS]) %></td>
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