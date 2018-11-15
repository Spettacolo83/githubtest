package it.stefanorussello.githubtest;

import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import it.stefanorussello.githubtest.adapters.RepoAdapter;
import it.stefanorussello.githubtest.listeners.RepoDetailsListener;
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
    private String reposURL = "https://api.github.com/repositories";
    private String searchURL = "https://api.github.com/users/%s/repos";
    private List<GithubRepo> listRepos;
    private GithubRepo selectedRepo;
    private ListView listviewRepos;
    private Utility utility;
    private SearchView searchView;
    private RepoAdapter repoAdapter;

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
            retrieveRepos(reposURL);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu_actions, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            final EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchPlate.setHint("Search GitHub user");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    utility.showLoading(MainActivity.this, getString(R.string.loading_repos));
                    if (query.length() > 0) {
                        retrieveRepos(String.format(searchURL, query));
                    } else {
                        retrieveRepos(reposURL);
                    }
                    searchPlate.setText("");
                    searchView.setIconified(true);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void retrieveRepos(String url) {

        listRepos = new ArrayList<>();
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                stopLoadingShowError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                utility.dismissLoading(MainActivity.this);
                if (response.isSuccessful()) {
                    JsonArray jsonRepos = new Gson().fromJson(response.body().string(), JsonArray.class);
                    listRepos = new Gson().fromJson(jsonRepos, new TypeToken<List<GithubRepo>>() {}.getType());

                    if (repoAdapter == null) {
                        repoAdapter = new RepoAdapter(MainActivity.this, listRepos, new RepoListener() {
                            @Override
                            public void itemClicked(int position, GithubRepo repo) {

                                utility.showLoading(MainActivity.this, getString(R.string.loading_repos));

                                retrieveRepoDetails(repo, new RepoDetailsListener() {
                                    @Override
                                    public void detailsDownloaded(GithubRepo repo) {
                                        selectedRepo = repo;
                                        String getCommitsUrl = selectedRepo.commitsUrl.replaceAll("\\{.*?\\}", "");
                                        retrieveCountDetails(selectedRepo, getCommitsUrl, new RepoDetailsListener() {
                                            @Override
                                            public void detailsDownloaded(GithubRepo repo) {
                                                String getBranchesUrl = selectedRepo.branchesUrl.replaceAll("\\{.*?\\}", "");
                                                retrieveCountDetails(selectedRepo, getBranchesUrl, new RepoDetailsListener() {
                                                    @Override
                                                    public void detailsDownloaded(GithubRepo repo) {
                                                        utility.dismissLoading(MainActivity.this);
                                                        Intent intentDetails = new Intent();
                                                        intentDetails.setClass(MainActivity.this, DetailsActivity.class);
                                                        intentDetails.putExtra("GithubRepo", new Gson().toJson(selectedRepo));
                                                        intentDetails.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intentDetails);
                                                    }

                                                    @Override
                                                    public void detailsFailed(String error) {
                                                        stopLoadingShowError();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void detailsFailed(String error) {
                                                stopLoadingShowError();
                                            }
                                        });
                                    }

                                    @Override
                                    public void detailsFailed(String error) {

                                    }
                                });
                            }
                        });
                    }
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listviewRepos.getAdapter() == null) {
                                listviewRepos.setAdapter(repoAdapter);
                            } else {
                                repoAdapter.refresh(listRepos);
                            }
                        }
                    });

                } else {
                    stopLoadingShowError();
                }
            }
        });
    }

    private void retrieveRepoDetails(final GithubRepo githubRepo, final RepoDetailsListener listener) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(githubRepo.url)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.detailsFailed(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    JsonObject jsonRepos = new Gson().fromJson(response.body().string(), JsonObject.class);
                    listener.detailsDownloaded(new Gson().fromJson(jsonRepos, githubRepo.getClass()));
                } else {
                    listener.detailsFailed(response.body().string());
                }
            }
        });
    }


    private void retrieveCountDetails(final GithubRepo repo, final String apiUrl, final RepoDetailsListener listener) {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.detailsFailed(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    JsonArray jsonCount = new Gson().fromJson(response.body().string(), JsonArray.class);
                    if (jsonCount != null) {
                        if (apiUrl.contains("commits")) {
                            repo.commits = jsonCount.size();
                        } else if (apiUrl.contains("branches")) {
                            repo.branches = jsonCount.size();
                        }
                    }
                    listener.detailsDownloaded(repo);

                } else {
                    listener.detailsFailed(response.body().string());
                }
            }
        });
    }

    private void stopLoadingShowError() {
        utility.dismissLoading(MainActivity.this);
        utility.showAlert(MainActivity.this, getString(R.string.error_get_feed_title), getString(R.string.error_get_feed_msg));
    }
}
