package hr.com.in.tempsync.ui.topics;

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
import java.util.Objects;

import hr.com.in.tempsync.R;
import hr.com.in.tempsync.api.data.DeviceGET;
import hr.com.in.tempsync.api.data.TopicWithReadingsGET;
import hr.com.in.tempsync.ui.rviewinterface.RecyclerViewOnClick;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {

    private final RecyclerViewOnClick recyclerViewOnClick;
    private final ArrayList<TopicWithReadingsGET> userTopics;
    private Context context;
    private SharedPreferences sharedPreferences;
    private final Gson gson;


    public TopicAdapter(Context context, RecyclerViewOnClick recyclerViewOnClick) {
        this.recyclerViewOnClick = recyclerViewOnClick;
        this.context = context;
        this.gson = new Gson();

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

            String jsonTopicsValue = this.sharedPreferences.getString("topics", "[{\"topicName\": \"Loading...\",\"readings\": [{\"value\": \"...\",\"timestamp\": \"2024-06-09T13:10:50.532+00:00\"}]}]");
            JsonArray topics = this.gson.fromJson(jsonTopicsValue, JsonArray.class);

            userTopics = new ArrayList<>();

            for(JsonElement topic : topics){
                TopicWithReadingsGET topicObject = gson.fromJson(topic, TopicWithReadingsGET.class);
                userTopics.add(topicObject);
            }
            userTopics.sort((t1, t2) -> {
                if(!t1.getDeviceName().equals(t2.getDeviceName())){
                    return t1.getDeviceName().compareTo(t2.getDeviceName());
                }else{
                    return t1.getTopicName().compareTo(t2.getTopicName());
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TopicAdapter(Context context, List<TopicWithReadingsGET> newTopics, RecyclerViewOnClick recyclerViewOnClick) {
        this.recyclerViewOnClick = recyclerViewOnClick;
        this.context = context;
        this.gson = new Gson();

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
            newTopics.sort((t1, t2) -> {
                if(!t1.getDeviceName().equals(t2.getDeviceName())){
                    return t1.getDeviceName().compareTo(t2.getDeviceName());
                }else{
                    return t1.getTopicName().compareTo(t2.getTopicName());
                }
            });

            String jsonValue = this.gson.toJson(newTopics, List.class);
            this.sharedPreferences.edit().putString("topics", jsonValue).apply();

            userTopics = new ArrayList<>(newTopics);

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView topicName, deviceName, readingValue, readingTime;
        public ViewHolder(@NonNull View itemView, RecyclerViewOnClick recyclerViewOnClick) {
            super(itemView);

            topicName = itemView.findViewById(R.id.topicItemName);
            deviceName = itemView.findViewById(R.id.topicDeviceName);
            readingTime = itemView.findViewById(R.id.topicReadingTime);
            readingValue = itemView.findViewById(R.id.topicReading);
        }

        public TextView getTopicName() {
            return topicName;
        }

        public TextView getDeviceName() {
            return deviceName;
        }

        public TextView getReadingValue() {
            return readingValue;
        }

        public TextView getReadingTime() {
            return readingTime;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_row_item, parent, false);

        return new ViewHolder(view, recyclerViewOnClick);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TopicWithReadingsGET topic = this.userTopics.get(position);

        holder.getTopicName().setText(topic.getTopicName());
        holder.getDeviceName().setText(topic.getDeviceName());
        holder.getReadingValue().setText(this.context.getString(R.string.reading_value_display, topic.getReadings().get(0).getValue(), "°C"));
        holder.getReadingTime().setText(this.context.getString(R.string.reading_time_display, topic.getReadings().get(0).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return this.userTopics.size();
    }


}
