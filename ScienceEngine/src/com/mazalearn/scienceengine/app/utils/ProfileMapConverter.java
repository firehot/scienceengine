package com.mazalearn.scienceengine.app.utils;

import java.util.HashMap;
import java.util.Map;

import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;
import com.mazalearn.scienceengine.app.services.ProfileData.Social;

public class ProfileMapConverter {

  public static Map<String, Object> profileToMap(ProfileData profile) {
    Map<String, Object> profileMap = new HashMap<String, Object>();
    profileMap.put(ProfileData.CLIENT_PROPS, profile.client);
    profileMap.put(ProfileData.SERVER_PROPS, profile.server);
    profileMap.put(ProfileData.LAST_UPDATED, profile.lastUpdated);
    profileMap.put(ProfileData.SOCIAL, profile.social);
    profileMap.put(ProfileData.COACH_PNG, profile.coachPng);
    profileMap.put(ProfileData.USER_PNG, profile.userPng);
    profileMap.put(ProfileData.TOPIC_STATS, profile.topicStats);
    return profileMap;
  }

  @SuppressWarnings("unchecked")
  public static void mapToProfile(ProfileData profile, Map<String, Object> myData) {
    profile.client = (ClientProps) myData.get(ProfileData.CLIENT_PROPS);
    profile.server = (ServerProps) myData.get(ProfileData.SERVER_PROPS);
    profile.lastUpdated = (HashMap<String, Long>) myData
        .get(ProfileData.LAST_UPDATED);
    profile.social = (Social) myData.get(ProfileData.SOCIAL);
    profile.coachPng = (String) myData.get(ProfileData.COACH_PNG);
    profile.userPng = (String) myData.get(ProfileData.USER_PNG);
    profile.topicStats = 
        (HashMap<String, HashMap<String, float[]>>) myData.get(ProfileData.TOPIC_STATS);
  }
}