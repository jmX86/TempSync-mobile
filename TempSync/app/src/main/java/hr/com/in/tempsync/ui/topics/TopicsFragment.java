package hr.com.in.tempsync.ui.topics;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import hr.com.in.tempsync.R;
import hr.com.in.tempsync.api.ApiClient;
import hr.com.in.tempsync.api.ApiService;
import hr.com.in.tempsync.api.data.TopicWithReadingsGET;
import hr.com.in.tempsync.databinding.FragmentTopicsBinding;
import hr.com.in.tempsync.ui.rviewinterface.RecyclerViewOnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopicsFragment extends Fragment implements RecyclerViewOnClick {
    private static final String TAG = "TOPICS_FRAGMENT";

    private FragmentTopicsBinding binding;

    Handler topicsHandler;
    Thread topicUpdateThread;
    ApiService apiService;
    TopicAdapter topicAdapter;
    SharedPreferences sharedPreferences;
    RecyclerView topicsRecyclerView;
    private View root;
    private boolean isTopicUpdateThreadRunning = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TopicsViewModel topicsViewModel =
                new ViewModelProvider(this).get(TopicsViewModel.class);

        binding = FragmentTopicsBinding.inflate(inflater, container, false);
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

        topicsRecyclerView = binding.topicsRecyclerView;
        topicsRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));

        topicAdapter = new TopicAdapter(root.getContext(), this);

        topicsRecyclerView.setAdapter(topicAdapter);

        topicsHandler = new Handler();

        apiService = ApiClient.getClient(root.getContext()).create(ApiService.class);

        topicUpdateThread = new Thread(getTopicUpdateRunnable());

        isTopicUpdateThreadRunning = true;

        topicUpdateThread.start();

        return root;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        this.isTopicUpdateThreadRunning = false;

        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemClick(int position) {

    }

    Runnable getTopicUpdateRunnable(){
        return () -> {
            Log.d(TAG, "Starting topic update thread.");
            while(isTopicUpdateThreadRunning){
                topicsHandler.post(() -> {
                    Call<List<TopicWithReadingsGET>> getTopics = apiService.getAllTopicsWithReadings();

                    getTopics.enqueue(new Callback<List<TopicWithReadingsGET>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<TopicWithReadingsGET>> call, @NonNull Response<List<TopicWithReadingsGET>> response) {
                            if (response.isSuccessful()){
                                if(response.body() == null){
                                    return;
                                }
                                Log.d(TAG, "Api call success");
                                Log.d(TAG, "Swapping adapters");
                                topicAdapter = new TopicAdapter(root.getContext(), response.body(), TopicsFragment.this);
                                topicsRecyclerView.swapAdapter(topicAdapter, true);

                            }else{
                                Log.d(TAG, "Api call not success.");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<TopicWithReadingsGET>> call, @NonNull Throwable t) {
                            Log.d(TAG, "Api call failed: " + t);
                        }
                    });
                });
                // Sleep for 1 min
                try {
                    Log.d(TAG, "Waiting 1min to make Api call");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        if(!isTopicUpdateThreadRunning){
            this.isTopicUpdateThreadRunning = true;
            this.topicUpdateThread = new Thread(getTopicUpdateRunnable());
            this.topicUpdateThread.start();
        }

        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        if(!isTopicUpdateThreadRunning){
            this.isTopicUpdateThreadRunning = true;
            this.topicUpdateThread = new Thread(getTopicUpdateRunnable());
            this.topicUpdateThread.start();
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        this.isTopicUpdateThreadRunning = false;
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");

        this.isTopicUpdateThreadRunning = false;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        this.isTopicUpdateThreadRunning = false;
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");

        this.isTopicUpdateThreadRunning = false;
        super.onDetach();
    }
}