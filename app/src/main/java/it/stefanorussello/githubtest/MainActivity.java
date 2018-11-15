package it.stefanorussello.githubtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import it.stefanorussello.githubtest.adapters.RepoAdapter;
import it.stefanorussello.githubtest.listeners.RepoListener;
import it.stefanorussello.githubtest.models.GithubRepo;
import it.stefanorussello.githubtest.utils.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.getName();
    private String settingURL = "https://api.github.com/repositories";
    private List<GithubRepo> listRepos;
    private ListView listviewRepos;
    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());

        utility = new Utility();

        listviewRepos = findViewById(R.id.listviewRepos);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (listRepos == null) {
            utility.showLoading(this, getString(R.string.loading_repos));
            retrieveRepos();
        }
    }

    private void retrieveRepos() {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(settingURL)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                utility.dismissLoading(MainActivity.this);
                utility.showAlert(MainActivity.this, getString(R.string.error_get_feed_title), getString(R.string.error_get_feed_msg));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                utility.dismissLoading(MainActivity.this);
                if (response.isSuccessful()) {
                    JsonArray jsonRepos = new Gson().fromJson(response.body().string(), JsonArray.class);
                    listRepos = new Gson().fromJson(jsonRepos, new TypeToken<List<GithubRepo>>() {}.getType());

                    final RepoAdapter adapter = new RepoAdapter(MainActivity.this, listRepos, new RepoListener() {
                        @Override
                        public void itemClicked(int position, GithubRepo repo) {
                            utility.showAlert(MainActivity.this, repo.name, repo.url);
                        }
                    });
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listviewRepos.setAdapter(adapter);
                        }
                    });

                } else {
                    utility.showAlert(MainActivity.this, getString(R.string.error_get_feed_title), getString(R.string.error_get_feed_msg));
                }
            }
        });
    }
}
