package com.socialmedia;

/**
 * Created by omoto on 25/8/16.
 */
public interface SocialMedia {

    //to connect social media
    void connect();
    //to post social media data
    Object post(String client);
    //save clint info
    void saveClientInfo();
    //social media request handler
    Object socialMediaRequestHandler(String serviceType, SocialMediaFactory socialMediaFactory,SocialMediaRequestJSON socialMediaRequestJSON);

}
