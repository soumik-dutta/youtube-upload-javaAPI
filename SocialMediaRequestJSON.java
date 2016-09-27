package com.socialmedia;

/**
 *
 * Created by Soumik on 27/8/16. $Created
 */
public class SocialMediaRequestJSON {
    private String userid;
    private String credentialDatastore;
    private String code;
    private String videoPath;


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getCredentialDatastore() {
        return credentialDatastore;
    }

    public void setCredentialDatastore(String credentialDatastore) {
        this.credentialDatastore = credentialDatastore;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }
}
