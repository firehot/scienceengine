/*package com.mazalearn.scienceengine.server;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class CheckUser {
  private boolean isUserAllowed() {
    List<String> authorizedEmails = 
        Arrays.asList(new String[] {
            "sridhar.sundaram@gmail.com",
        });
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    return authorizedEmails.contains(user.getEmail());
  }
} */
