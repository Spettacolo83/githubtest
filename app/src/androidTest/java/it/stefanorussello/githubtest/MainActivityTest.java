package it.stefanorussello.githubtest;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import it.stefanorussello.githubtest.listeners.RepoCountListener;
import it.stefanorussello.githubtest.listeners.ReposListener;
import it.stefanorussello.githubtest.models.Branch;
import it.stefanorussello.githubtest.models.Commit;
import it.stefanorussello.githubtest.models.GithubRepo;

import static org.junit.Assert.assertTrue;
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
    public void testPublicRepos() {
        mainActivity.retrieveRepos(mainActivity.reposURL, new ReposListener() {
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

    @Test
    @UiThreadTest
    public void testFacebookRepo() {

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

    @Test
    public void testRepoCounting() {

        ArrayList<Branch> branchTest = new ArrayList<>();
        Branch branch = new Branch();
        branch.name = "master";
        branch.commit = new Commit();
        branch.commit.sha = "f8263af6894fd595698eb9d4f69712cdd0f28f25";
        branch.commit.url = "https://api.github.com/repos/facebook/360-Capture-SDK/commits/f8263af6894fd595698eb9d4f69712cdd0f28f25";
        branchTest.add(branch);

        mainActivity.retrieveCommitCounts(branchTest, new RepoCountListener() {
            @Override
            public void repoCounted(int totalCount) {
                assertTrue(totalCount > 0);
            }

            @Override
            public void repoFailed(String error) {
                fail("Test branch repo didn't count as expected!");
            }
        });
    }

    @Test
    public void testSHACommit() {
        String branchSHAurl = "https://api.github.com/repos/facebook/360-Capture-SDK/commits?per_page=100&page=1&sha=f8263af6894fd595698eb9d4f69712cdd0f28f25";
        try {
            List<Commit> commits = mainActivity.getSHACommits(branchSHAurl);
            assertTrue(commits.size() > 0);
        } catch (Exception e) {
            fail("Unexpected commit response!");
        }
    }

    @After
    public void tearDown() throws Exception {
        mainActivity = null;
    }
}