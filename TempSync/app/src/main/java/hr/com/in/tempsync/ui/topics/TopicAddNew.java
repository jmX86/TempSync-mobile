package hr.com.in.tempsync.ui.topics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hr.com.in.tempsync.AppActivity;
import hr.com.in.tempsync.R;
import hr.com.in.tempsync.api.ApiClient;
import hr.com.in.tempsync.api.ApiService;
import hr.com.in.tempsync.api.data.TopicGET;
import hr.com.in.tempsync.api.data.TopicPOST;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopicAddNew extends AppCompatActivity {
    private static final String TAG = "ADD_NEW_TOPIC";
    EditText etDeviceName, etTopicName;
    Button btnConfirm, btnBack;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_topic_add_new);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etDeviceName = findViewById(R.id.etTopicDeviceName);
        etTopicName = findViewById(R.id.etNewTopicName);

        btnConfirm = findViewById(R.id.buttonTopicAddConfirm);
        btnBack = findViewById(R.id.buttonTopicAddBack);

        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        btnBack.setOnClickListener(v -> {
            Intent backToAppActivity = new Intent(getApplicationContext(), AppActivity.class);
            startActivity(backToAppActivity);
        });

        btnConfirm.setOnClickListener(v -> {
            String deviceName = etDeviceName.getText().toString();
            String topicName = etTopicName.getText().toString();

            if(deviceName.isEmpty()){
                return;
            }
            if(topicName.isEmpty()){
                return;
            }

            Call<TopicGET> newTopicCall = apiService.postNewTopic(new TopicPOST(topicName, deviceName, 0L, 1L));
            newTopicCall.enqueue(new Callback<TopicGET>() {
                @Override
                public void onResponse(@NonNull Call<TopicGET> call, @NonNull Response<TopicGET> response) {
                    if(response.isSuccessful()){
                        Log.d(TAG, "Topic added");
                        Toast.makeText(getApplicationContext(), "Topic added", Toast.LENGTH_SHORT).show();
                        etTopicName.setText("");
                    }else{
                        Log.d(TAG, "Topic not added" + response);
                        Toast.makeText(getApplicationContext(), "Topic add fail", Toast.LENGTH_SHORT).show();
                    }
                    btnConfirm.setClickable(true); // Enable click
                }

                @Override
                public void onFailure(@NonNull Call<TopicGET> call, @NonNull Throwable t) {
                    Log.d(TAG, "Topic add failure" + t);
                    Toast.makeText(getApplicationContext(), "Fail" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    btnConfirm.setClickable(true); // Enable click
                }
            });
        });
    }
}