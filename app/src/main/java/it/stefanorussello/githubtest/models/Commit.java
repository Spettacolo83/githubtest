package it.stefanorussello.githubtest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Commit {

    @SerializedName("sha")
    @Expose
    public String sha;
    @SerializedName("url")
    @Expose
    public String url;

}