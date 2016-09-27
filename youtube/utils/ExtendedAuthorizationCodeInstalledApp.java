package com.socialmedia.google.youtube.utils;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.Gson;
import com.socialmedia.SocialMediaConstants;
import com.socialmedia.google.youtube.json.GoogleRequestJSON;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Extented class of AuthorizationCodeInstalledApp
 * Contain methods like public method
 * @see com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
 * @edit 2016-09-26T15:04:59.376Z
 * Created by soumik .
 */
public class ExtendedAuthorizationCodeInstalledApp extends AuthorizationCodeInstalledApp implements SocialMediaConstants {
    /**
     * @param flow     authorization code flow
     * @param receiver verification code receiver
     */
    /** Authorization code flow. */
    public final AuthorizationCodeFlow flow;

    /** Verification code receiver. */
    public final VerificationCodeReceiver receiver;

    public ExtendedAuthorizationCodeInstalledApp(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
        super(flow, receiver);
        this.flow = Preconditions.checkNotNull(flow);
        this.receiver = Preconditions.checkNotNull(receiver);
    }


    /**
     * Authorizes the installed application to access user's protected data.
     * @param userId user ID or {@code null} if not using a persisted credential store
     * @return credential
     */
    public Credential authorize(String userId) throws IOException {
        try {
            //get the credentials from the stored credentials file
            Credential credential = flow.loadCredential(userId);
            if (credential != null
                    && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() > 60)) {
                return credential;
            }
            //otherwise get from the google authentication
            // open in browser
            String redirectUri = receiver.getRedirectUri();

            //set your custom name-value pair
            AuthorizationCodeRequestUrl authorizationUrl =flow.newAuthorizationUrl().setRedirectUri(redirectUri);
            //print the url that is to be opened by the user and authenticate.
            //System.out.println(authorizationUrl);

            onAuthorization(authorizationUrl);
            // receive authorization code and exchange it for an access token
            String code = receiver.waitForCode();
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            // store credential and return it
            return flow.createAndStoreCredential(response, userId);
        } finally {
            receiver.stop();
        }
    }


    /**
     * Get access token from stored credentials
     * @param userId lookup the credential file by the userid specified.
     * @return credential object or null if there is no such userid exits
     */
    public Credential getAuthorizationFromStorage(String userId){
        try {
            //get credentials from storage
            Credential credential=flow.loadCredential(userId);

            /* check whether the credential object and refresh token is not null as then it can generate new accesstoken
             * other wise of the access token is not expired  */
            if(credential!=null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() > 60))
                return credential;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the authentication with the credentials from Google
     * creates  the url that will lead the user to the authentication page
     * and creating a custom defined name-value pair in the state parameter.
     * The value should be encoded with base64 encoder so we can receive the
     * same code redirected from google after authentication.
     * @link http://stackoverflow.com/questions/7722062/google-oauth2-redirect-uri-with-several-parameters
     * @param userid
     * @param credentialDatastore
     * @author soumik 2016-09-27T11:00:40.312Z
     */
    public void getAuthorizationFromGoogle(String userid, String credentialDatastore) throws IOException {

        //callback url after google authentication
        String redirectUri=GOOGLE_CALLBACK;

        //create the Json String that will send along with the other parameters
        GoogleRequestJSON requestJSON=new GoogleRequestJSON();
        requestJSON.setUserid(userid);
        requestJSON.setCredentialDatastore(credentialDatastore);
        String request= new Gson().toJson(requestJSON);

         /*Inside state parameter we can encode our custom parameter that
          * will be received after callback*/
        AuthorizationCodeRequestUrl authorizationUrl =flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .set("state", Base64.encodeBase64String(request.getBytes()));

        onAuthorization(authorizationUrl);

    }


    /**
     * Save the credentials that we get from  google.
     * 1.create the GoogleAuthorizationCodeFlow object from the credentialDatastore
     *   from the response received from the google after authentication.
     * 2.Hit google to get the permanent refresh-token that can be used to get the
     *   accesstoken of the user any time .
     * 3.Store the tokens like accesstoken and refreshtoken in the filename as userid
     * @param userid it will be send in the googlecallback url
     * @param credentialDatastore it will be send in the googlecallback url
     * @param code it will be send in the googlecallback url
     * @return the credential object
     * @throws URISyntaxException
     * @throws IOException
     * @author soumik 2016-09-27T11:00:54.747Z
     */
    public Credential saveAuthorizationFromGoogle(String userid,String credentialDatastore,String code) throws URISyntaxException, IOException {
        // Load client secrets.
        URI filePath = new URI(GOOGLE_APIKEY);
        Reader clientSecretReader = new InputStreamReader(new FileInputStream(filePath.toString()));

        //Reader clientSecretReader = new InputStreamReader(Auth.class.getResourceAsStream(GOOGLE_APIKEY));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

        // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
        DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);


        //Builds a login URL based on client ID, secret, callback URI, and scopes
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .setApprovalPrompt("auto")
                .setCredentialDataStore(datastore)
                .build();

        //get the refreshtoken from google as soon as we get the code from google
        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(GOOGLE_CALLBACK).execute();

        //save the credentials that we got the tokenresponse.
        Credential credential=flow.createAndStoreCredential(response,userid);

        return credential;

    }
}