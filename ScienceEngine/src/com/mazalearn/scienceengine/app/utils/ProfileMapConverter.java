package com.mazalearn.scienceengine.app.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.mazalearn.scienceengine.app.services.ProfileData;
import com.mazalearn.scienceengine.app.services.ProfileData.ClientProps;
import com.mazalearn.scienceengine.app.services.ProfileData.ServerProps;

public class ProfileMapConverter {

  private Gson gson = new Gson();
  public Map<String, Object> profileToMap(ProfileData profile) {
    Map<String, Object> profileMap = new HashMap<String, Object>();
    profileMap.put(ProfileData.CLIENT_PROPS, gson.toJson(profile.client));
    profileMap.put(ProfileData.SERVER_PROPS, gson.toJson(profile.server));
    profileMap.put(ProfileData.LAST_UPDATED, profile.lastUpdated);
    // profileMap.put(ProfileData.SOCIAL, profile.social);
    profileMap.put(ProfileData.COACH_PNG, profile.coachPng);
    profileMap.put(ProfileData.USER_PNG, profile.userPng);
    profileMap.put(ProfileData.TOPIC_STATS, profile.topicStats);
    return profileMap;
  }

  @SuppressWarnings("unchecked")
  public ProfileData mapToProfile(HashMap<String, Object> profileMap) {
    ProfileData profile = new ProfileData();
    profile.client = gson.fromJson((String) profileMap.get(ProfileData.CLIENT_PROPS), ClientProps.class);
    profile.server = gson.fromJson((String) profileMap.get(ProfileData.SERVER_PROPS), ServerProps.class);
    profile.lastUpdated = (HashMap<String, Long>) profileMap
        .get(ProfileData.LAST_UPDATED);
    // profile.social = (Social) profileMap.get(ProfileData.SOCIAL);
    profile.coachPng = (String) profileMap.get(ProfileData.COACH_PNG);
    profile.userPng = (String) profileMap.get(ProfileData.USER_PNG);
    profile.topicStats = (HashMap<String, HashMap<String, float[]>>) profileMap
        .get(ProfileData.TOPIC_STATS);
    return profile;
  }
}