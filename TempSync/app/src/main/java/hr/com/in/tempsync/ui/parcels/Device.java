package hr.com.in.tempsync.ui.parcels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

import hr.com.in.tempsync.api.data.DeviceGET;
import hr.com.in.tempsync.api.data.TopicGET;

public class Device implements Parcelable {
    private final String deviceName;
    private ArrayList<String> topics;

    public Device(DeviceGET device){
        this.deviceName = device.getDeviceName();
        this.topics = new ArrayList<>();
        for(TopicGET topic : device.getTopics()){
            topics.add(topic.getTopicName());
        }
        Collections.sort(topics);
    }

    protected Device(Parcel in) {
        this.deviceName = in.readString();
        topics = new ArrayList<>();
        in.readStringList(topics);
        Collections.sort(topics);
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.deviceName);
        dest.writeStringList(this.topics);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public ArrayList<String> getTopics() {
        return topics;
    }
}
