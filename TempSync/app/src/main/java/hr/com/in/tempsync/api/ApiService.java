package hr.com.in.tempsync.api;

import java.util.List;

import hr.com.in.tempsync.MainActivity;
import hr.com.in.tempsync.R;
import hr.com.in.tempsync.api.data.DeviceGET;
import hr.com.in.tempsync.api.data.DevicePOST;
import hr.com.in.tempsync.api.data.ReadingGET;
import hr.com.in.tempsync.api.data.TopicGET;
import hr.com.in.tempsync.api.data.TopicPOST;
import hr.com.in.tempsync.api.data.TopicWithReadingsGET;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    String api_prefix = "/tempsync/api";

    @GET(api_prefix + "/login")
    Call<Void> tryLogin();

    @GET(api_prefix + "/device")
    Call<List<DeviceGET>> getDevices();
    @POST(api_prefix + "/device")
    Call<DeviceGET> postNewDevice(@Body DevicePOST device);
    @GET(api_prefix + "/device/{deviceName}/{topicName}/latest")
    Call<ReadingGET> getReadingFor(@Path("deviceName") String deviceName, @Path("topicName") String topicName);

    @GET(api_prefix + "/topic")
    Call<List<TopicWithReadingsGET>> getAllTopicsWithReadings();



    @POST(api_prefix + "/topic")
    Call<TopicGET> postNewTopic(@Body TopicPOST topic);

    @DELETE(api_prefix + "/device/{deviceName}")
    Call<Void> deleteDevice(@Path("deviceName") String deviceName);
}
