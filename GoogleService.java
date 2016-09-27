package com.socialmedia.google;

import com.socialmedia.SocialMedia;
import com.socialmedia.SocialMediaConstants;
import com.socialmedia.SocialMediaFactory;
import com.socialmedia.SocialMediaRequestJSON;

/**
 * Created by omoto on 24/8/16.
 */
public class GoogleService implements SocialMedia,SocialMediaConstants {
    public GoogleService() {
        System.out.println("Google service called ....");
    }

    @Override
    public void connect() {

    }

    @Override
    public Object post(String client) {

        return null;
    }

    @Override
    public void saveClientInfo() {

    }


    /**
     * handle google services from here
     * @param serviceType
     * @param socialMediaFactory
     * @param socialMediaRequestJSON
     */
    @Override
    public Object socialMediaRequestHandler(String serviceType, SocialMediaFactory socialMediaFactory,SocialMediaRequestJSON socialMediaRequestJSON) {
        SocialMedia socialMedia;
        System.out.println("Inside Google service ......");
        if (serviceType.equalsIgnoreCase(YOUTUBE)) {
            socialMedia=socialMediaFactory.getSocialMediaObject(serviceType);
            Object returnedVideo=socialMedia.socialMediaRequestHandler(serviceType,socialMediaFactory,socialMediaRequestJSON);
            return returnedVideo;
        }else {
            return null;
        }
    }
}
