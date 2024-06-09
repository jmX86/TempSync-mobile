package hr.com.in.tempsync.api.interceptors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import hr.com.in.tempsync.R;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BasicAuthInterceptor implements Interceptor {

    private final SharedPreferences sharedPreferences;

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        String userName = sharedPreferences.getString("username", "abc-abc");
        String userPass = sharedPreferences.getString("password", "abc-abc");

        //Log.d("BASIC_AUTH", userName + ":" + userPass);

        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", Credentials.basic(userName, userPass))
                .build();

        return chain.proceed(authenticatedRequest);
    }

    public BasicAuthInterceptor(Context context){
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            this.sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    context.getString(R.string.preferences_name),
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Log.d("BASIC_AUTH", "sharedPreferences: " + sharedPreferences);

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
