package it.stefanorussello.githubtest.listeners;

import java.io.IOException;

import it.stefanorussello.githubtest.models.GithubRepo;

public interface RepoDetailsListener {
    public void detailsDownloaded(GithubRepo repo);
    public void detailsFailed(String error);
}
