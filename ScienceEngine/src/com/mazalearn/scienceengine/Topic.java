package com.mazalearn.scienceengine;

import java.util.HashMap;
import java.util.Map;


public enum Topic {
  BarMagnet(102, "Magnetic field due to a bar magnet - compass, field lines of force, rotation"),
  Field(101, "Electromagnetic field and its effect on magnetic materials and electric charges"),
  TwoWires(103, "Magnetic field due to wires carrying current - how field changes with current"),
  ElectroMagnet(104, "Electromagnet and its field change with coil loops, loop area and current"),
  BarMagnetInduction(105, "Nature of current induced in a coil due to a moving bar magnet - Faradays laws"),
  ElectroMagnetInduction(106, "Current induced when an electromagnet moves near a coil and parameters affecting"),
  DCMotor(107, "Working of DC and AC motors - current carrying coil, magnet, commutator"),
  ElectromagnetismScienceTrain(109, "Create your own coach with dynamo and share with friends"),
  ElectromagnetismReview(108, "Concepts in electromagnetism reviewed through multiple choice questions"),
  Electromagnetism(1, "All Levels - Concepts in magnetic fields, electromagnets, dynamos and motors",
      BarMagnet, BarMagnet, Field, TwoWires, ElectroMagnet, BarMagnetInduction, ElectroMagnetInduction, DCMotor,
      ElectromagnetismReview, ElectromagnetismScienceTrain),
  SOM(201, "States of Matter basic"),
  StatesOfMatterReview(210, "Concepts in States Of Matter reviewed through multiple choice questions"),
  StatesOfMatter(2, "All Levels - Concepts in States of Matter",
      SOM, SOM, StatesOfMatterReview), 
  W(301, "Waves - basic"),
  WavesReview(310, "Concepts in Waves reviewed through multiple choice questions"),
  Waves(3, "All Levels - Concepts in Waves",
      W, W, WavesReview);

  private static final String PRODUCT_PREFIX = "com.mazalearn.scienceengine.";
  private final Topic[] childTopics;
  private final Topic canonicalChild;
  private final int topicId;
  private final String description;
  private boolean isFree = false;
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
  Topic(int topicId, String description, Topic... childTopics) {
    this.topicId = topicId;
    this.description = description;
    if (childTopics.length == 0) {
      this.canonicalChild = null;
      this.childTopics = childTopics;
      return;
    }
    this.canonicalChild = childTopics[0];
    this.canonicalChild.isFree = true;
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
  
  public String getDescription() {
    return description;
  }
  
  public boolean isFree() {
    return isFree;
  }
  
  public Topic idToTopic(int topicId) {
    return idToTopicMap.get(topicId);
  }
  
  public static Topic fromProductId(String productId) {
    if (!productId.startsWith(PRODUCT_PREFIX)) {
      return null;
    }
    
    String prodId = productId.substring(PRODUCT_PREFIX.length());
    for (Topic topic: values()) {
      if (prodId.equals(topic.name().toLowerCase())) {
        return topic;
      }
    }
    
    return null;
  }

  public String toProductId() {
    return PRODUCT_PREFIX + name().toLowerCase();
  }
}
