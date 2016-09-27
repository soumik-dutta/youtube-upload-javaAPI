package com.socialmedia.google.youtube;

import com.omoto.constants.Constants;
import com.socialmedia.SocialMedia;
import com.socialmedia.SocialMediaFactory;
import com.socialmedia.SocialMediaRequestJSON;
import com.socialmedia.google.youtube.json.GoogleRequestJSON;
import com.socialmedia.google.youtube.youtubeHelper.UploadVideoHelper;
import com.socialmedia.google.youtube.youtubeHelper.YoutubeRequestJSON;

/**
 * Created by omoto on 24/8/16.
 */
public class YoutubeService implements SocialMedia,Constants {
    UploadVideoHelper uploadVideoHelper;
    SocialMediaRequestJSON youtubeRequestJSON;
    GoogleRequestJSON requestJSON;

    public YoutubeService(){
        System.out.println("Youtube service constructor called ..");
    }

    @Override
    public void connect() {

    }

    @Override
    public Object post(String client) {
        uploadVideoHelper=new UploadVideoHelper();
        //String uploadVideoName=youtubeRequestJSON.getVideopath();
        Object returnedVideo=uploadVideoHelper.uploadVideo(youtubeRequestJSON);
        return returnedVideo;
    }

    @Override
    public void saveClientInfo() {

    }

    //handle youtube service from here
    @Override
    public Object socialMediaRequestHandler(String serviceType,SocialMediaFactory socialMediaFactory,SocialMediaRequestJSON socialMediaRequestJSON) {
        System.out.println("Inside youtube socialMediaRequestHandler ....");
        //casting socialMediaRequestJSON to youtubeRequestJSON
        youtubeRequestJSON=  socialMediaRequestJSON;
        //post method to upload video
        Object returnedVideo= post(socialMediaRequestJSON.getUserid());
        return returnedVideo;
    }




}
