package hr.com.in.tempsync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import hr.com.in.tempsync.api.ApiClient;
import hr.com.in.tempsync.api.ApiService;
import hr.com.in.tempsync.databinding.ActivityAppBinding;
import hr.com.in.tempsync.ui.devices.DeviceAddNew;
import hr.com.in.tempsync.ui.topics.TopicAddNew;

public class AppActivity extends AppCompatActivity {

    private ActivityAppBinding binding;

    private Toolbar appToolbar;

    private Context context;
    SharedPreferences sharedPreferences;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = getApplicationContext();

        apiService = ApiClient.getClient(this.context).create(ApiService.class);

        try {
            MasterKey masterKey = new MasterKey.Builder(this.context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    this.context,
                    getString(R.string.preferences_name),
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        binding = ActivityAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appToolbar = findViewById(R.id.appToolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // TODO: Uncomment when implemented
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_devices, R.id.navigation_topics, R.id.navigation_connections)
//                .build();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_devices, R.id.navigation_topics)
                .build();

        setSupportActionBar(appToolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_app);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        int logoutId = R.id.toolbarLogoutMenuItem;
        int addDeviceId = R.id.toolbarAddNewDevice;
        int addTopicId = R.id.toolbarAddNewTopic;
        int addConnId = R.id.toolbarAddNewConnection;
        int userInfoId = R.id.toolbarUserInfo;

        if(itemId == logoutId){
            sharedPreferences.edit().clear().apply();
            Intent logoutToLoginScreenIntent = new Intent(this.context, MainActivity.class);
            startActivity(logoutToLoginScreenIntent);
            return true;
        }
        if(itemId == addDeviceId){
            Intent addNewDeviceActivity = new Intent(getApplicationContext(), DeviceAddNew.class);
            startActivity(addNewDeviceActivity);
            return true;
        }
        if(itemId == addTopicId){
            Intent addNewTopicActivity = new Intent(getApplicationContext(), TopicAddNew.class);
            startActivity(addNewTopicActivity);
            return true;
        }
        if(itemId == addConnId){
            Toast.makeText(getApplicationContext(), "Coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }
        if(itemId == userInfoId){
            Toast.makeText(getApplicationContext(), "Coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}