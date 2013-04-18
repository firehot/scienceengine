package com.mazalearn.scienceengine;

import java.util.HashMap;
import java.util.Map;


public enum Topic {
  BarMagnet(102), Field(101), TwoWires(103), ElectroMagnet(104),
  BarMagnetInduction(105), ElectroMagnetInduction(106),
  DCMotor(107), ScienceTrain(109), EMReview(108),
  Electromagnetism(1, BarMagnet, Field, BarMagnet, TwoWires, ElectroMagnet, BarMagnetInduction, ElectroMagnetInduction, DCMotor,
      EMReview, ScienceTrain),
  SOM(201), StatesOfMatter(2, SOM, SOM), 
  W(301), Waves(3, W, W);

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
