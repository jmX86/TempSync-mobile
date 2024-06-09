package hr.com.in.tempsync.ui.devices;

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
import hr.com.in.tempsync.api.data.DeviceGET;
import hr.com.in.tempsync.api.data.DevicePOST;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceAddNew extends AppCompatActivity {
    private static final String TAG = "ADD_NEW_DEVICE";
    EditText etDeviceName;
    Button btnConfirm, btnBack;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_add_new);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etDeviceName = findViewById(R.id.editTextNewDeviceName);
        btnBack = findViewById(R.id.buttonTopicAddBack);
        btnConfirm = findViewById(R.id.buttonTopicAddConfirm);

        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        btnBack.setOnClickListener(v -> {
            Intent backToAppActivity = new Intent(getApplicationContext(), AppActivity.class);
            startActivity(backToAppActivity);
        });

        btnConfirm.setOnClickListener(v -> {
            String newDeviceName = etDeviceName.getText().toString();
            if(newDeviceName.isEmpty()){
                Toast.makeText(getApplicationContext(), "No name given", Toast.LENGTH_SHORT).show();
                return;
            }
            if(newDeviceName.length() > 64){
                Toast.makeText(getApplicationContext(), "Name too long", Toast.LENGTH_SHORT).show();
                return;
            }
            if(newDeviceName.contains("/") || newDeviceName.contains("#") || newDeviceName.contains("+")){
                Toast.makeText(getApplicationContext(), "Invalid char in name", Toast.LENGTH_SHORT).show();
                return;
            }
            Call<DeviceGET> newDeviceCall = apiService.postNewDevice(new DevicePOST(newDeviceName));
            btnConfirm.setClickable(false); // Disable click
            newDeviceCall.enqueue(new Callback<DeviceGET>() {
                @Override
                public void onResponse(@NonNull Call<DeviceGET> call, @NonNull Response<DeviceGET> response) {
                    if(response.isSuccessful()){
                        Log.d(TAG, "Device added");
                        Toast.makeText(getApplicationContext(), "Device added", Toast.LENGTH_SHORT).show();
                        etDeviceName.setText("");
                    }else{
                        Log.d(TAG, "Device not added" + response);
                        Toast.makeText(getApplicationContext(), "Device add fail", Toast.LENGTH_SHORT).show();
                    }
                    btnConfirm.setClickable(true); // Enable click
                }

                @Override
                public void onFailure(@NonNull Call<DeviceGET> call, @NonNull Throwable t) {
                    Log.d(TAG, "Device add failure" + t);
                    Toast.makeText(getApplicationContext(), "Fail" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    btnConfirm.setClickable(true); // Enable click
                }
            });
        });
    }
}