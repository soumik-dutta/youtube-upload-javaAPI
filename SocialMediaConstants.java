package com.socialmedia;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.common.collect.Lists;


import java.util.List;

/**
 * Created by omoto on 26/8/16.
 */
public interface SocialMediaConstants {
    public static String GOOGLE ="google";
    public static String YOUTUBE ="youtube";
    public static String FACEBOOK ="facebook";

    //api-keys for social-media
    public static final String GOOGLE_APIKEY = "/home/omoto/api_keys/google/client_secret.json";
    public static final String SOCIALMEDIA = "socialmedia";

    //google redirection callback
    public static  final String GOOGLE_CALLBACK="http://demo.omoto.io/omoto/googlecallback.htm";

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";
    public static final List<String> SCOPES = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");

}
