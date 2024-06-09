package hr.com.in.tempsync.ui.devices;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import hr.com.in.tempsync.R;
import hr.com.in.tempsync.api.ApiClient;
import hr.com.in.tempsync.api.ApiService;
import hr.com.in.tempsync.api.data.DeviceGET;
import hr.com.in.tempsync.api.data.ReadingGET;
import hr.com.in.tempsync.api.data.TopicGET;
import hr.com.in.tempsync.databinding.FragmentDevicesBinding;
import hr.com.in.tempsync.ui.parcels.Device;
import hr.com.in.tempsync.ui.rviewinterface.RecyclerViewOnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DevicesFragment extends Fragment implements RecyclerViewOnClick {

    public static final String TAG = "DEVICE_FRAGMENT";
    private FragmentDevicesBinding binding;

    Handler devicesHandler;

    Thread deviceUpdateThread, deviceReadingsUpdateThread;
    Runnable deviceUpdateRunnable, readingsUpdateRunnable;

    ApiService apiService;

    DeviceAdapter deviceAdapter;

    SharedPreferences sharedPreferences;

    boolean isDeviceUpdateThreadRunning = false;
    boolean isReadingsUpdateThreadRunning = false;

    ArrayList<DeviceGET> devicesDisplayed;

    RecyclerView devicesRecyclerView;

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        DevicesViewModel devicesViewModel =
                new ViewModelProvider(this).get(DevicesViewModel.class);

        binding = FragmentDevicesBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        try {
            MasterKey masterKey = new MasterKey.Builder(root.getContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    root.getContext(),
                    getString(R.string.preferences_name),
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        devicesRecyclerView = binding.devicesRecyclerView;
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        deviceAdapter = new DeviceAdapter(root.getContext(), this);
        devicesDisplayed = deviceAdapter.getDevicesDisplayed();

        devicesRecyclerView.setAdapter(deviceAdapter);

        devicesHandler = new Handler();

        apiService = ApiClient.getClient(root.getContext()).create(ApiService.class);

        deviceUpdateThread = new Thread(getDeviceUpdateRunnable());

        deviceReadingsUpdateThread = new Thread(getReadingsUpdateRunnable());

        isDeviceUpdateThreadRunning = true;
        isReadingsUpdateThreadRunning = true;

        deviceUpdateThread.start();
        deviceReadingsUpdateThread.start();

        return root;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");

        this.isDeviceUpdateThreadRunning = false;
        this.isReadingsUpdateThreadRunning = false;
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        if(!isReadingsUpdateThreadRunning){
            this.isReadingsUpdateThreadRunning = true;
            this.deviceReadingsUpdateThread = new Thread(getReadingsUpdateRunnable());
            this.deviceReadingsUpdateThread.start();
        }

        if(!isDeviceUpdateThreadRunning){
            this.isDeviceUpdateThreadRunning = true;
            this.deviceUpdateThread = new Thread(getDeviceUpdateRunnable());
            this.deviceUpdateThread.start();
        }

        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        if(!isReadingsUpdateThreadRunning){
            this.isReadingsUpdateThreadRunning = true;
            this.deviceReadingsUpdateThread = new Thread(getReadingsUpdateRunnable());
            this.deviceReadingsUpdateThread.start();
        }

        if(!isDeviceUpdateThreadRunning){
            this.isDeviceUpdateThreadRunning = true;
            this.deviceUpdateThread = new Thread(getDeviceUpdateRunnable());
            this.deviceUpdateThread.start();
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        this.isDeviceUpdateThreadRunning = false;
        this.isReadingsUpdateThreadRunning = false;
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");

        this.isDeviceUpdateThreadRunning = false;
        this.isReadingsUpdateThreadRunning = false;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        this.isDeviceUpdateThreadRunning = false;
        this.isReadingsUpdateThreadRunning = false;
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");

        this.isDeviceUpdateThreadRunning = false;
        this.isReadingsUpdateThreadRunning = false;
        super.onDetach();
    }

    @Override
    public void onItemClick(int position) {
        Intent showDeviceInfo = new Intent(getContext(), DeviceMoreInfo.class);
        showDeviceInfo.putExtra("device", new Device(deviceAdapter.getDevicesDisplayed().get(position)));

        startActivity(showDeviceInfo);
    }

    Runnable getDeviceUpdateRunnable(){
        return () -> {
            Log.d(TAG, "Starting device update thread.");
            while(isDeviceUpdateThreadRunning){
                devicesHandler.post(() -> {
                    Call<List<DeviceGET>> getDevicesCall = apiService.getDevices();

                    getDevicesCall.enqueue(new Callback<List<DeviceGET>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<DeviceGET>> call, @NonNull Response<List<DeviceGET>> response) {
                            if(response.isSuccessful()){
                                Log.d(TAG, "Api call success");

                                assert response.body() != null;
                                if(!deviceAdapter.deviceListUnchanged(response.body())){
                                    Log.d(TAG, "Swapping adapters");
                                    deviceAdapter = new DeviceAdapter(root.getContext(), response.body(), DevicesFragment.this);
                                    devicesRecyclerView.swapAdapter(deviceAdapter, true);
                                    devicesDisplayed = deviceAdapter.getDevicesDisplayed();
                                }else{
                                    Log.d(TAG, "Devices unchanged");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<DeviceGET>> call, @NonNull Throwable t) {
                            Log.d(TAG, "Api call failed: " + t);
                        }
                    });
                });
                // Sleep for 1 min
                try {
                    Log.d(TAG, "Waiting 1min to make Api call");
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    Runnable getReadingsUpdateRunnable(){
        return () -> {
            Log.d(TAG, "Starting readings update thread.");
            while(isReadingsUpdateThreadRunning){
                devicesHandler.post(() -> {
                    // Getting readings and calling adapter.notifyItemChanged
                    for(DeviceGET d : deviceAdapter.getDevicesDisplayed()){
                        for(TopicGET t : d.getTopics()){
                            Call<ReadingGET> readingGet = apiService.getReadingFor(d.getDeviceName(), t.getTopicName());
                            readingGet.enqueue(new Callback<ReadingGET>() {
                                @Override
                                public void onResponse(@NonNull Call<ReadingGET> call, @NonNull Response<ReadingGET> response) {
                                    if(response.isSuccessful()){
                                        Log.d(TAG, "Api call success");
                                        assert response.body() != null;
                                        long newTimestampMillis = response.body().getTimestampInt();
                                        long oldTimestampMillis = sharedPreferences.getLong("readings."+d.getDeviceName()+"."+t.getTopicName()+".i", 0);
                                        if(newTimestampMillis != oldTimestampMillis) {
                                            Log.d(TAG, "Updating reading: " + d.getDeviceName() + "/" + t.getTopicName());
                                            sharedPreferences.edit().putLong("readings." + d.getDeviceName() + "." + t.getTopicName() + ".i", response.body().getTimestampInt()).apply();
                                            sharedPreferences.edit().putString("readings." + d.getDeviceName() + "." + t.getTopicName() + ".v", response.body().getValue()).apply();
                                            sharedPreferences.edit().putString("readings." + d.getDeviceName() + "." + t.getTopicName() + ".t", response.body().getTimestamp()).apply();

                                            deviceAdapter.notifyItemChanged(devicesDisplayed.indexOf(d));
                                        }else{
                                            Log.d(TAG, "Reading already displayed.");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ReadingGET> call, @NonNull Throwable t) {
                                    Log.d(TAG, "Api call fail: " + t);
                                }
                            });
                        }
                    }
                });
                try {
                    Log.d(TAG, "Sleeping in Readings thread for 10s");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}