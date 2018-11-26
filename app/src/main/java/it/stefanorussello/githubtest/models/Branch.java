package it.stefanorussello.githubtest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Branch {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("commit")
    @Expose
    public Commit commit;

}