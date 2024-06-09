package hr.com.in.tempsync.ui.devices;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import hr.com.in.tempsync.R;
import hr.com.in.tempsync.api.data.DeviceGET;
import hr.com.in.tempsync.ui.rviewinterface.RecyclerViewOnClick;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private final RecyclerViewOnClick recyclerViewOnClick;

    private final ArrayList<DeviceGET> userDevices;

    Context context;

    SharedPreferences sharedPreferences;

    private final Gson gson;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView deviceName, topicName, readingValue, readingTime;

        public ViewHolder(@NonNull View itemView, RecyclerViewOnClick recyclerViewOnClick) {
            super(itemView);

            deviceName = itemView.findViewById(R.id.deviceItemName);
            topicName = itemView.findViewById(R.id.deviceTopicName);
            readingValue = itemView.findViewById(R.id.deviceTopicReading);
            readingTime = itemView.findViewById(R.id.deviceTopicReadingTime);

            itemView.setOnClickListener((view) -> {
                if(recyclerViewOnClick != null){
                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION){
                        recyclerViewOnClick.onItemClick(position);
                    }
                }
            });
        }

        public TextView getDeviceNameTextView() {
            return deviceName;
        }

        public TextView getTopicNameTextView() {
            return topicName;
        }

        public TextView getReadingValueTextView(){ return readingValue; }

        public TextView getReadingTimeTextView() {
            return readingTime;
        }
    }

    public DeviceAdapter(Context context, RecyclerViewOnClick recyclerViewOnClick){
        this.context = context;
        this.gson = new Gson();
        this.recyclerViewOnClick = recyclerViewOnClick;

        try {
            MasterKey masterKey = new MasterKey.Builder(this.context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    this.context,
                    this.context.getString(R.string.preferences_name),
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String jsonDevicesValue = this.sharedPreferences.getString("devices", "[{\"deviceName\": \"Loading...\",\"topics\": [{\"topicName\": \"...\"}]}]");
            JsonArray devices = this.gson.fromJson(jsonDevicesValue, JsonArray.class);

            userDevices = new ArrayList<>();

            for(JsonElement device : devices){
                DeviceGET deviceObject = gson.fromJson(device, DeviceGET.class);
                userDevices.add(deviceObject);
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DeviceAdapter(Context context, List<DeviceGET> devicesList, RecyclerViewOnClick recyclerViewOnClick){
        this.context = context;
        this.gson = new Gson();
        this.recyclerViewOnClick = recyclerViewOnClick;

        try {
            MasterKey masterKey = new MasterKey.Builder(this.context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    this.context,
                    this.context.getString(R.string.preferences_name),
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            sharedPreferences.edit().putString("devices", gson.toJson(devicesList)).apply();

            userDevices = new ArrayList<>(devicesList);

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_row_item, parent, false);

        return new ViewHolder(view, recyclerViewOnClick);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceAdapter.ViewHolder holder, int position) {
        DeviceGET device = this.userDevices.get(position);
        String topicToRead = sharedPreferences.getString("readings."+device.getDeviceName()+".preferred.topic", "<NO_TOPIC>");

        if(topicToRead.equals("<NO_TOPIC>")){
            if(!device.getTopics().isEmpty()){
                topicToRead = device.getTopics().get(0).getTopicName();
            }
        }

        String readingValue = sharedPreferences.getString("readings." + device.getDeviceName() + "." + topicToRead + ".v", "Not loaded");
        String readingTime = sharedPreferences.getString("readings." + device.getDeviceName() + "." + topicToRead + ".t", "Not loaded");

        holder.getDeviceNameTextView().setText(device.getDeviceName());
        holder.getTopicNameTextView().setText(topicToRead);
        holder.getReadingValueTextView().setText(context.getString(R.string.reading_value_display, readingValue, "°C"));
        holder.getReadingTimeTextView().setText(context.getString(R.string.reading_time_display, readingTime));
    }

    @Override
    public int getItemCount() {
        return this.userDevices.size();
    }

    public ArrayList<DeviceGET> getDevicesDisplayed(){
        return this.userDevices;
    }

    public boolean deviceListUnchanged(List<DeviceGET> newDeviceList){
        for(DeviceGET d : newDeviceList){
            if(!this.userDevices.contains(d)){
                return false;
            }
        }
        return true;
    }
}
