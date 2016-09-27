package com.socialmedia;


import com.socialmedia.facebook.FacebookService;
import com.socialmedia.google.GoogleService;
import com.socialmedia.google.youtube.YoutubeService;

/**
 * Factory pattern implementation of Social media object
 * Created by soumik on 25/8/16.
 */
public  class SocialMediaFactory implements SocialMediaConstants {


    public SocialMedia socialMedia;

    /**
     * get the social media object
     * @param socialMediaType
     * @return
     */
    public SocialMedia getSocialMediaObject(String socialMediaType){
        if(socialMediaType.equalsIgnoreCase(FACEBOOK)) {
            socialMedia = new FacebookService();
        }
        if(socialMediaType.equalsIgnoreCase(GOOGLE)) {
            socialMedia = new GoogleService();
        }
        if(socialMediaType.equalsIgnoreCase(YOUTUBE)) {
            socialMedia = new YoutubeService();
        }

        return socialMedia;
    }

}
