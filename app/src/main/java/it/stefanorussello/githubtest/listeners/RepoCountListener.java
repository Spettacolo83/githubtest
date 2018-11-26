package it.stefanorussello.githubtest.listeners;

public interface RepoCountListener {
    public void repoCounted(int totalCount);
    public void repoFailed(String error);
}
