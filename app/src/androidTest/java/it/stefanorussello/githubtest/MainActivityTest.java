package it.stefanorussello.githubtest;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import it.stefanorussello.githubtest.listeners.ReposListener;
import it.stefanorussello.githubtest.models.GithubRepo;

import static org.junit.Assert.fail;

public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private MainActivity mainActivity = null;

    @Before
    public void setUp() throws Exception {
        mainActivity = mainActivityActivityTestRule.getActivity();
    }

    @Test
    @UiThreadTest
    public void testFacebookRepo() {

        mainActivity = mainActivityActivityTestRule.getActivity();

        String strRepo = "Facebook";
        final String urlApi = String.format(mainActivity.searchURL, strRepo);

        mainActivity.retrieveRepos(urlApi, new ReposListener() {
            @Override
            public void reposDownloaded(List<GithubRepo> repos) {
                Assert.assertTrue(repos.size() > 0);
            }

            @Override
            public void reposFailed(String error) {
                fail("Facebook repo didn't download as expected!");
            }
        });

    }

    @After
    public void tearDown() throws Exception {
        mainActivity = null;
    }
}