package com.interset.util;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
public class Document {
    public int getVoteCount() {
        return voteCount;
    }
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
    public float getPopularity() {
        return Popularity;
    }
    public void setPopularity(float popularity) {
        Popularity = popularity;
    }
    public float getVoteAverage() {
        return voteAverage;
    }
    public void setVoteAverage(float voteAverage) {this.voteAverage = voteAverage;}
    public String getTitle() {
        return Title;
    }
    public void setTitle(String title) {
        Title = title;
    }
    public String getOverview() {
        return Overview;
    }
    public void setOverview(String overview) {
        Overview = overview;
    }
    public String getReleaseDate() {return releaseDate;}
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @SerializedName("Vote Count")int voteCount;
    @SerializedName("Popularity")float Popularity;
    @SerializedName("Vote Average")float voteAverage;
    @SerializedName("Title")String Title;
    @SerializedName("Overview")String Overview;
    @SerializedName("Release Date")String releaseDate;

}