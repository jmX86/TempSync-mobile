package hr.com.in.tempsync.api.data;

import com.google.gson.annotations.SerializedName;

public class DevicePOST {
    @SerializedName("deviceName")
    String deviceName;

    public DevicePOST(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
