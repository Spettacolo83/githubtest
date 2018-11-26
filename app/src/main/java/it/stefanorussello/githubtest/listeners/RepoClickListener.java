package it.stefanorussello.githubtest.listeners;

import it.stefanorussello.githubtest.models.GithubRepo;

public interface RepoClickListener {
    public void itemClicked(int position, GithubRepo repo);
}

