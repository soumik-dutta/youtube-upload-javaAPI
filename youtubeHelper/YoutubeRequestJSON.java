package com.socialmedia.google.youtube.youtubeHelper;

import com.socialmedia.SocialMediaRequestJSON;

/**
 * Created by omoto on 27/8/16.
 */
public class YoutubeRequestJSON  {
    //path of the video that is to be uploaded
    private String videopath;

    private String code;



    //getter-setter
    public String getVideopath() {
        return videopath;
    }

    public void setVideopath(String videopath) {
        this.videopath = videopath;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
