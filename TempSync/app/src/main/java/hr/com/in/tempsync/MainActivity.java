package hr.com.in.tempsync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

import hr.com.in.tempsync.api.ApiClient;
import hr.com.in.tempsync.api.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    Boolean credentialsAvailable = false;
    String userName;

    Context context;

    ApiService apiService;

    SharedPreferences sharedPreferences;

    Button loginButton, logoutButton;

    EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();

        apiService = ApiClient.getClient(this.context).create(ApiService.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton = findViewById(R.id.buttonLogin);
        logoutButton = findViewById(R.id.buttonLogout);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);

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

        if(sharedPreferences.contains("username") && sharedPreferences.contains("password")){
            credentialsAvailable = true;
            userName = sharedPreferences.getString("username", "----");
            loginButton.setText(context.getString(R.string.btn_continue_as, userName));
            logoutButton.setVisibility(View.VISIBLE);
            logoutButton.setClickable(true);
        }else{
            credentialsAvailable = false;
            loginButton.setText(context.getString(R.string.btn_login));
            logoutButton.setVisibility(View.GONE);
            logoutButton.setClickable(false);
        }

        loginButton.setOnClickListener(v -> {
            if(!credentialsAvailable) {
                String userName = usernameEditText.getText().toString();
                String userPass = passwordEditText.getText().toString();

                sharedPreferences.edit().putString("username", userName).apply();
                sharedPreferences.edit().putString("password", userPass).apply();
            }

            Call<Void> loginCall = apiService.tryLogin();
            loginCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if(response.isSuccessful()) {
                        Log.d("LOGIN", "OK");
                        Toast.makeText(context, "Login OK", Toast.LENGTH_SHORT).show();

                        Intent switchToAppIntent = new Intent(context, AppActivity.class);
                        startActivity(switchToAppIntent);
                    }else{
                        Log.d("LOGIN", "FAIL: " + response);
                        Toast.makeText(context, "Login FAIL", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(context, "Login FAIL", Toast.LENGTH_SHORT).show();
                    Log.d("LOGIN", "FAIL: " + t);
                }
            });
        });

        logoutButton.setOnClickListener(v -> {
            sharedPreferences.edit().remove("username").remove("password").apply();
            this.recreate();
        });
    }
}