package hr.com.in.tempsync.api.data;

public class TopicGET {
    private String topicName;

    public TopicGET(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
