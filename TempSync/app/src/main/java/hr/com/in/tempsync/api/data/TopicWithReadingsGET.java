package hr.com.in.tempsync.api.data;

import java.util.List;

public class TopicWithReadingsGET {
    private String topicName;
    private String deviceName;
    private List<ReadingGET> readings;

    public TopicWithReadingsGET(String topicName, String deviceName, List<ReadingGET> readings) {
        this.topicName = topicName;
        this.deviceName = deviceName;
        this.readings = readings;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public List<ReadingGET> getReadings() {
        return readings;
    }

    public void setReadings(List<ReadingGET> readings) {
        this.readings = readings;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
