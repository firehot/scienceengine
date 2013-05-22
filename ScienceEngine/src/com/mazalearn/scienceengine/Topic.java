package com.mazalearn.scienceengine;

import java.util.HashMap;
import java.util.Map;


public enum Topic {
  BarMagnet(102), Field(101), TwoWires(103), ElectroMagnet(104),
  BarMagnetInduction(105), ElectroMagnetInduction(106),
  DCMotor(107), ElectromagnetismScienceTrain(109), ElectromagnetismReview(108),
  Electromagnetism(1, BarMagnet, BarMagnet, Field, TwoWires, ElectroMagnet, BarMagnetInduction, ElectroMagnetInduction, DCMotor,
      ElectromagnetismReview, ElectromagnetismScienceTrain),
  SOM(201), StatesOfMatterReview(210), StatesOfMatter(2, SOM, SOM, StatesOfMatterReview), 
  W(301), WavesReview(310), Waves(3, W, W, WavesReview);

  private final Topic[] childTopics;
  private final Topic canonicalChild;
  private final int topicId;
  private static Map<Integer, Topic> idToTopicMap = new HashMap<Integer, Topic>();
  
  static {
    for (Topic topic: values()) {
      idToTopicMap.put(topic.getTopicId(), topic);
    }
  }
  /**
   * Constructor
   * @param topicId - numerical id for topic used for stats
   * @param childTopics - canonicalchild as first followed by all child topics in order
   */
  Topic(int topicId, Topic... childTopics) {
    this.topicId = topicId;
    if (childTopics.length == 0) {
      this.canonicalChild = null;
      this.childTopics = childTopics;
      return;
    }
    this.canonicalChild = childTopics[0];
    this.childTopics = new Topic[childTopics.length - 1];
    for (int i = 1; i < childTopics.length; i++) {
      this.childTopics[i - 1] = childTopics[i];
    }
  }
  
  public Topic getCanonicalChild() {
    return canonicalChild;
  }
  
  public Topic[] getChildren() {
    return childTopics;
  }

  public int getTopicId() {
    return topicId;
  }
  
  public Topic idToTopic(int topicId) {
    return idToTopicMap.get(topicId);
  }
}
