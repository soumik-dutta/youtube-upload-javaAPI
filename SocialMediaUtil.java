package com.socialmedia;


/**
 * Created by omoto on 27/9/16.
 */
public class SocialMediaUtil implements SocialMediaConstants {

    /**
     * Creating the video object
     * @param socialMediaRequestJSON
     * @param socialMediaType
     * @param socialMediaFactory
     * @param serviceType
     * @return
     */
     Object callSocialMediaService(SocialMediaRequestJSON socialMediaRequestJSON,String socialMediaType,SocialMediaFactory socialMediaFactory,String serviceType){
        SocialMedia socialMedia;
        if(socialMediaType.equalsIgnoreCase(GOOGLE)) {
            socialMedia = socialMediaFactory.getSocialMediaObject(socialMediaType);
            Object returnedVideo=socialMedia.socialMediaRequestHandler(serviceType,socialMediaFactory,socialMediaRequestJSON);
            return returnedVideo;
        }else {
            return null;
        }
    }
}
