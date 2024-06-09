package hr.com.in.tempsync.ui.devices;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.PrecomputedText;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

import hr.com.in.tempsync.AppActivity;
import hr.com.in.tempsync.R;
import hr.com.in.tempsync.api.ApiClient;
import hr.com.in.tempsync.api.ApiService;
import hr.com.in.tempsync.api.data.DeviceGET;
import hr.com.in.tempsync.api.data.ReadingGET;
import hr.com.in.tempsync.api.data.TopicGET;
import hr.com.in.tempsync.ui.parcels.Device;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceMoreInfo extends AppCompatActivity {
    RadioGroup topicGroup;

    TextView deviceNameTextView;

    Button backButton, confirmButton, deleteButton;

    SharedPreferences sharedPreferences;

    Handler readingHandler;
    Thread readingsUpdater;
    ApiService apiService;

    private String deviceName;
    private ArrayList<String> deviceTopics = new ArrayList<>();
    private boolean isReadingsUpdateThreadRunning = false;

    private HashMap<String, Integer> radioButtonTopicAndId = new HashMap<String, Integer>();

    private static final String TAG = "DEVICE_MORE_INFO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_more_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        readingHandler = new Handler();
        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        deviceNameTextView = findViewById(R.id.deviceMoreInfoTitle);
        backButton = findViewById(R.id.buttonDeviceInfoBack);
        confirmButton = findViewById(R.id.buttonDeviceInfoConfirm);
        deleteButton = findViewById(R.id.deviceMoreInfoDelete);

        Intent intent = getIntent();
        Device devicePassed = intent.getParcelableExtra("device");
        assert devicePassed != null;
        String deviceNameToDisplay = devicePassed.getDeviceName();
        this.deviceName = devicePassed.getDeviceName();
        this.deviceTopics = devicePassed.getTopics();

        deviceNameTextView.setText(deviceNameToDisplay);

        topicGroup = findViewById(R.id.deviceMoreInfoRadioGroup);

        try {
            MasterKey masterKey = new MasterKey.Builder(getApplicationContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    getApplicationContext(),
                    getApplicationContext().getString(R.string.preferences_name),
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        setupRadioButtons(this.deviceTopics);

        backButton.setOnClickListener(v -> {
            Intent backIntent = new Intent(getApplicationContext(), AppActivity.class);
            startActivity(backIntent);
        });

        confirmButton.setOnClickListener(v -> {
            int checkedRadioButton = topicGroup.getCheckedRadioButtonId();

            if(checkedRadioButton != -1){
                RadioButton selectedBtn = findViewById(topicGroup.getCheckedRadioButtonId());

                String topicNameAndReading = selectedBtn.getText().toString();
                String topicName = topicNameAndReading.split(" ")[0];

                sharedPreferences.edit().putString("readings."+this.deviceName+".preferred.topic", topicName).apply();

                Toast.makeText(getApplicationContext(), topicName + " selected.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Select one topic to change default", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            Call<Void> delDevice = apiService.deleteDevice(deviceNameToDisplay);
            delDevice.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if(response.isSuccessful()){
                        Toast.makeText(getApplicationContext(), deviceNameToDisplay + " deleted.", Toast.LENGTH_SHORT).show();
                        Intent back = new Intent(getApplicationContext(), AppActivity.class);
                        startActivity(back);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getApplicationContext(), deviceNameToDisplay + " delete fail.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Delete failed" + t);
                }
            });
        });

        setupReadingsUpdaterThread();

        isReadingsUpdateThreadRunning = true;
        this.readingsUpdater.start();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        isReadingsUpdateThreadRunning = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        isReadingsUpdateThreadRunning = false;
        super.onDestroy();
    }

    void setupRadioButtons(ArrayList<String> deviceTopicsToDisplay){
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(0, 5, 0, 5);

        for(String topicName : deviceTopicsToDisplay){
            RadioButton rbtn = new RadioButton(this);

            int radioButtonId = TextView.generateViewId();

            radioButtonTopicAndId.put(topicName, radioButtonId);

            String readingValue = sharedPreferences.getString("readings." + deviceName + "." + topicName + ".v", "0");

            rbtn.setId(radioButtonId);
            rbtn.setText(getString(R.string.reading_value_display, topicName, readingValue));
            rbtn.setTextSize(20);
            rbtn.setLayoutParams(params);
            this.topicGroup.addView(rbtn);
        }
    }

    void setupReadingsUpdaterThread(){
        this.readingsUpdater = new Thread(()->{
            while(isReadingsUpdateThreadRunning){
                readingHandler.post(()->{
                    for(String topic : this.deviceTopics){
                        Call<ReadingGET> readingGet = apiService.getReadingFor(deviceName, topic);
                        readingGet.enqueue(new Callback<ReadingGET>() {
                            @Override
                            public void onResponse(@NonNull Call<ReadingGET> call, @NonNull Response<ReadingGET> response) {
                                Log.d(TAG, "Api request success.");
                                if(response.body() != null) {
                                    long newTimestampMillis = response.body().getTimestampInt();
                                    long oldTimestampMillis = sharedPreferences.getLong("readings." + deviceName + "." + topic + ".i", 0);
                                    if (newTimestampMillis != oldTimestampMillis) {
                                        Log.d(TAG, "Updating reading: " + deviceName + "/" + topic);
                                        sharedPreferences.edit().putLong("readings." + deviceName + "." + topic + ".i", response.body().getTimestampInt()).apply();
                                        sharedPreferences.edit().putString("readings." + deviceName + "." + topic + ".v", response.body().getValue()).apply();
                                        sharedPreferences.edit().putString("readings." + deviceName + "." + topic + ".t", response.body().getTimestamp()).apply();

                                        try {
                                            int radioButtonUpdateId = radioButtonTopicAndId.getOrDefault(topic, 0);
                                            RadioButton radioButtonToChange = topicGroup.findViewById(radioButtonUpdateId);

                                            radioButtonToChange.setText(getApplicationContext().getString(R.string.reading_value_display, topic, response.body().getValue()));
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "Id of radio button to update is 0.");
                                        }
                                    } else {
                                        Log.d(TAG, "Reading already displayed.");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ReadingGET> call, @NonNull Throwable t) {

                            }
                        });
                    }
                });
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}