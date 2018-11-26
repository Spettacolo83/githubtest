package it.stefanorussello.githubtest;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import it.stefanorussello.githubtest.listeners.RepoClickListener;
import it.stefanorussello.githubtest.listeners.RepoCountListener;
import it.stefanorussello.githubtest.listeners.RepoDetailsListener;
import it.stefanorussello.githubtest.listeners.ReposListener;
import it.stefanorussello.githubtest.models.Branch;
import it.stefanorussello.githubtest.models.Commit;
import it.stefanorussello.githubtest.models.GithubRepo;
import it.stefanorussello.githubtest.utils.BasicAuthInterceptor;
import it.stefanorussello.githubtest.utils.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static String TAG = MainActivity.class.getName();
    public String reposURL = "https://api.github.com/repositories";
    public String searchURL = "https://api.github.com/users/%s/repos";
    private static final int REQUEST_LOGIN = 1;
    private List<GithubRepo> listRepos;
    private GithubRepo selectedRepo;
    private ListView listviewRepos;
    private Utility utility;
    private SearchView searchView;
    private RepoAdapter repoAdapter;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());

        utility = new Utility();

        listviewRepos = findViewById(R.id.listviewRepos);

        SharedPreferences prefs = getSharedPreferences("Login", MODE_PRIVATE);
        if (prefs.contains("username") && prefs.contains("password")) {
            username = prefs.getString("username", "");
            password = prefs.getString("password", "");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (listRepos == null) {
            utility.showLoading(this, getString(R.string.loading_repos));

            callAPI(reposURL);
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
                    String urlCall = reposURL;
                    if (query.length() > 0) {
                        reposURL = String.format(searchURL, query);
                    }

                    callAPI(reposURL);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_login) {
            Intent intentDetails = new Intent();
            intentDetails.setClass(MainActivity.this, LoginActivity.class);
            intentDetails.putExtra("username", username);
            intentDetails.putExtra("password", password);
//            intentDetails.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intentDetails, REQUEST_LOGIN);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                username = data.getStringExtra("username");
                password = data.getStringExtra("password");

                SharedPreferences.Editor editor = getSharedPreferences("Login", MODE_PRIVATE).edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.apply();
            }
        }
    }

    public void callAPI(String strURL) {
        retrieveRepos(strURL, new ReposListener() {
            @Override
            public void reposDownloaded(List<GithubRepo> repos) {
                listRepos = repos;
                setRepoAdapter(listRepos);
            }

            @Override
            public void reposFailed(String error) {
                stopLoadingShowError(error);
            }
        });
    }

    public void retrieveRepos(String url, final ReposListener listener) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (username != null && password != null && username.length() > 0 && password.length() > 0) {
            httpClient.addInterceptor(new BasicAuthInterceptor(username, password));
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        httpClient.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.reposFailed(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                utility.dismissLoading(MainActivity.this);
                if (response.isSuccessful()) {
                    JsonArray jsonRepos = new Gson().fromJson(response.body().string(), JsonArray.class);
                    ArrayList<GithubRepo> list = new ArrayList<>();
                    list = new Gson().fromJson(jsonRepos, new TypeToken<List<GithubRepo>>() {}.getType());

                    listener.reposDownloaded(list);

                } else {
                    listener.reposFailed(null);
                }
            }
        });
    }

    private void retrieveRepoDetails(final GithubRepo githubRepo, final RepoDetailsListener listener) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (username != null && password != null && username.length() > 0 && password.length() > 0) {
            httpClient.addInterceptor(new BasicAuthInterceptor(username, password));
        }
        Request request = new Request.Builder()
                .url(githubRepo.url)
                .build();
        httpClient.build().newCall(request).enqueue(new Callback() {
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
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (username != null && password != null && username.length() > 0 && password.length() > 0) {
            httpClient.addInterceptor(new BasicAuthInterceptor(username, password));
        }
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();
        httpClient.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.detailsFailed(e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    JsonArray jsonBranches = new Gson().fromJson(response.body().string(), JsonArray.class);
                    List<Branch> branches = new Gson().fromJson(jsonBranches, new TypeToken<List<Branch>>() {}.getType());
                    repo.branches = branches.size();

                    if (repo.branches > 0) {
                        retrieveCommitCounts(branches, new RepoCountListener() {
                            @Override
                            public void repoCounted(int totalCount) {
                                repo.commits = totalCount;

                                listener.detailsDownloaded(repo);
                            }

                            @Override
                            public void repoFailed(String error) {
                                listener.detailsFailed(error);
                            }
                        });
                    }

                } else {
                    listener.detailsFailed(response.body().string());
                }
            }
        });
    }

    public void retrieveCommitCounts(List<Branch> branches, RepoCountListener listener) {

        int totalCount = 0;
        int perPage = 100;

        for (Branch branch : branches) {
            String branchSHA = branch.commit.sha;
            int page = 1;
            boolean nextCommits = true;
            String commitsUrl = branch.commit.url.replace("/" + branch.commit.sha, "?per_page=" + perPage + "&page=" + page + "&sha=" + branchSHA);

            do {
                List<Commit> commits;
                try {
                    commits = getSHACommits(commitsUrl);
                } catch (Exception e) {
                    listener.repoFailed(e.getLocalizedMessage());
                    return;
                }

                if (commits != null && commits.size() > 0) {
                    totalCount = totalCount + commits.size();

                    // If commits count < pagination size, next page does not exist
                    nextCommits = commits.size() >= perPage;

                    if (nextCommits) {
                        page++;
                        commitsUrl = branch.commit.url.replace("/" + branch.commit.sha, "?per_page=" + perPage + "&page=" + page + "&sha=" + branchSHA);
                    }
                } else {
                    nextCommits = false;
                }

            } while (nextCommits);
        }

        listener.repoCounted(totalCount);
    }

    public List<Commit> getSHACommits(String url) throws Exception {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (username != null && password != null && username.length() > 0 && password.length() > 0) {
            httpClient.addInterceptor(new BasicAuthInterceptor(username, password));
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = httpClient.build().newCall(request).execute();

        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        JsonArray jsonCommits = new Gson().fromJson(response.body().string(), JsonArray.class);
        List<Commit> commits = new Gson().fromJson(jsonCommits, new TypeToken<List<Commit>>() {}.getType());

        return commits;
    }

    private void setRepoAdapter(final List<GithubRepo> list) {

        if (repoAdapter == null) {
            repoAdapter = new RepoAdapter(MainActivity.this, list, new RepoClickListener() {
                @Override
                public void itemClicked(int position, GithubRepo repo) {

                    utility.showLoading(MainActivity.this, getString(R.string.loading_repos));

                    retrieveRepoDetails(repo, new RepoDetailsListener() {
                        @Override
                        public void detailsDownloaded(GithubRepo repo) {
                            selectedRepo = repo;
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
                                    stopLoadingShowError(error);
                                }
                            });
                        }

                        @Override
                        public void detailsFailed(String error) {
                            stopLoadingShowError(error);
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
                    repoAdapter.refresh(list);
                }
            }
        });
    }

    private void stopLoadingShowError(String message) {
        if (message == null) message = getString(R.string.error_get_feed_msg); // Generic error if message is null
        utility.dismissLoading(MainActivity.this);
        utility.showAlert(MainActivity.this, getString(R.string.error_get_feed_title), message);
    }
}
