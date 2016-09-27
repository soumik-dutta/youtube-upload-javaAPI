package com.socialmedia;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.gson.Gson;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Entrypoint for any social media activity
 * @see com.omoto.constants.Constants FACEBOOK YOUTUBE
 * Created by soumik on 24/8/16.
 */
public class SocialMediaController implements Controller,SocialMediaConstants {
    /**
     * @socialMediaType is the social media that is to be accessed
     * @serviceType is the service of the social media
     * @socialMediaFactory is implementing the factory pattern
     */
    private String socialMediaType;
    private String serviceType;
    private SocialMediaFactory socialMediaFactory;
    private SocialMediaUtil socialMediaUtil;


    /**
     * for spring custructor injection
     * for method call we need to call explicitly
     * @param socialMediaType
     * @param serviceType
     * @param socialMediaFactory
     */
    public SocialMediaController(String socialMediaType, String serviceType, SocialMediaFactory socialMediaFactory, SocialMediaUtil socialMediaUtil) {
        this.socialMediaType=socialMediaType;
        this.socialMediaFactory=socialMediaFactory;
        this.serviceType=serviceType;
        this.socialMediaUtil=socialMediaUtil;
    }


    /**
     * Web response when socialmedia is called from URL
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("Inside Social media WebRequest controller......");
        String client=request.getParameter("client");
        String code=request.getParameter("code");
        SocialMediaRequestJSON requestJSON=null;

        String state=request.getParameter("state");
        if(state!=null)
            client=state;
        if(state!=null) {
            byte[] decodedStringByte = Base64.decodeBase64(state);
            requestJSON = new Gson().fromJson(new String(decodedStringByte), SocialMediaRequestJSON.class);
            requestJSON.setCode(code);
            System.out.println(requestJSON.getUserid() + "  " + requestJSON.getCredentialDatastore());
        }




        //this needs to properly set when integrating it with web
//        SocialMediaRequestJSON socialMediaRequestJSON= (SocialMediaRequestJSON) request.getHeaders(SOCIALMEDIA);
        Object object=socialMediaUtil.callSocialMediaService(requestJSON,socialMediaType,socialMediaFactory,serviceType);

        response.setStatus(200);
//        response.addHeader("Access-Control-Allow-Origin", "*");
//        response.setContentType("application/x-json");
//        response.setContentLength(responseString.getBytes("UTF8").length);
//        PrintWriter writer =  new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);
//        writer.write(responseString);
//        writer.close();

        return null;
    }



    /**
     * Method Expose that will be called directly from another method in the application
     * @param socialMediaRequestJSON
     * @return
     */
    public Object handleRequest(SocialMediaRequestJSON socialMediaRequestJSON){
        System.out.println("Inside Social media MethodRequest controller......");
        //empty string to be changed to client
        Object object=socialMediaUtil.callSocialMediaService(socialMediaRequestJSON,socialMediaType,socialMediaFactory,serviceType);

        return object;
    }
}
