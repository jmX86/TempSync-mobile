package hr.com.in.tempsync.api.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class DeviceGET {
    @SerializedName("deviceName")
    private String deviceName;

    @SerializedName("topics")
    private List<TopicGET> topics;

    public DeviceGET(String deviceName, List<TopicGET> topics) {
        this.deviceName = deviceName;
        this.topics = topics;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public List<TopicGET> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicGET> topics) {
        this.topics = topics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceGET deviceGET = (DeviceGET) o;
        return Objects.equals(deviceName, deviceGET.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceName);
    }
}
