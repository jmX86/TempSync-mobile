package hr.com.in.tempsync.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.security.crypto.EncryptedSharedPreferences;

import com.google.gson.internal.GsonBuildConfig;

import hr.com.in.tempsync.MainActivity;
import hr.com.in.tempsync.R;
import hr.com.in.tempsync.api.interceptors.BasicAuthInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static Retrofit getClient(Context context){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new BasicAuthInterceptor(context))
                .build();

        return new Retrofit.Builder()
                .baseUrl(context.getString(R.string.api_base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
}
