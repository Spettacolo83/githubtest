package it.stefanorussello.githubtest.listeners;

import java.util.List;

import it.stefanorussello.githubtest.models.GithubRepo;

public interface ReposListener {
    public void reposDownloaded(List<GithubRepo> repos);
    public void reposFailed(String error);
}
