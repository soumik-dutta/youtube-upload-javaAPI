package com.socialmedia.google.youtube.json;

import com.socialmedia.SocialMediaRequestJSON;

/**
 * Created by omoto on 26/9/16.
 */
public class GoogleRequestJSON {
    private String userid;
    private String credentialDatastore;
    private String code;

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
}
