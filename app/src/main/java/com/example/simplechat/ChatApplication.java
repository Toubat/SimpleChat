package com.example.simplechat;

import android.app.Application;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import com.parse.Parse;
import com.parse.ParseObject;

public class ChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Message.class);

        // Use for monitoring Parse network traffic
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        // Any network interceptors must be added with the Configuration Builder given this syntax
        builder.networkInterceptors().add(httpLoggingInterceptor);

        Parse.initialize(new Parse.Configuration.Builder(this)
             .applicationId("IT9CFEu9H3rj1FHLMckJVUfExqCKZ3k4CWzpCj6L")
             .clientKey("tWEHG16AVN7KpuePR9NCjf7lLTzItE7DUmFeaLvB")
             .clientBuilder(builder)
             .server("https://parseapi.back4app.com").build());
    }
}
