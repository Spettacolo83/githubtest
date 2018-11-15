package it.stefanorussello.githubtest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import it.stefanorussello.githubtest.models.GithubRepo;
import it.stefanorussello.githubtest.utils.Utility;

public class DetailsActivity extends Activity {

    private Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        utility = new Utility();

        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtStars = findViewById(R.id.txtStars);
        TextView txtCommits = findViewById(R.id.txtCommits);
        TextView txtForks = findViewById(R.id.txtForks);
        TextView txtBranches = findViewById(R.id.txtBranches);

        Button btnOpenRepo = findViewById(R.id.btnOpenRepo);

        final GithubRepo githubRepo = new Gson().fromJson(getIntent().getStringExtra("GithubRepo"), GithubRepo.class);

        if (githubRepo != null) {
            txtTitle.setText(githubRepo.name);
            if (githubRepo.stargazersCount != null) txtStars.setText(String.valueOf(githubRepo.stargazersCount));
            txtCommits.setText(String.valueOf(githubRepo.commits));
            if (githubRepo.forksCount != null) txtForks.setText(String.valueOf(githubRepo.forksCount));
            txtBranches.setText(String.valueOf(githubRepo.branches));
        } else {
            utility.showAlert(this, getString(R.string.error_get_feed_title), getString(R.string.error_get_feed_msg));
        }

        btnOpenRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (githubRepo != null) {
                    Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubRepo.htmlUrl));
                    startActivity(browseIntent);
                }
            }
        });
    }
}
