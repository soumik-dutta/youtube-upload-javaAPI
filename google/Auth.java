package com.socialmedia.google;

/**
 * Created by omoto on 24/8/16.
 */

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.Gson;
import com.socialmedia.SocialMediaConstants;
import com.socialmedia.google.youtube.json.GoogleRequestJSON;
import com.socialmedia.google.youtube.utils.ExtendedAuthorizationCodeInstalledApp;



import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Shared class used by every sample. Contains methods for authorizing a user and caching credentials.
 */
public class Auth implements SocialMediaConstants {

    /**
     * Define a global instance of the HTTP transport.
     */


    public static final String CLIENTID="146760935634-nif2ddpj70f2js1pt8ogpucho9npfguo.apps.googleusercontent.com";

    public static final String CLIENT_SECRET="146760935634-nif2ddpj70f2js1pt8ogpucho9npfguo.apps.googleusercontent.com";


    /**
     * Define a global instance of the JSON factory.
     */


    /**
     * This is the directory that will be used under the user's home directory where OAuth tokens will be stored.
     */


    public static final String REFRESH_TOKEN="1/Jk_Zd_sOTF1LLy36TgATigUgQyBBnGWb_XHwEgWPN5k";

    LocalServerReceiver localReceiver=null;

    /**
     * Authorizes the installed application to access user's protected data.
     *
     * @param scopes              list of scopes needed to run youtube upload.
     * @param credentialDatastore name of the credential datastore to cache OAuth tokens
     */
    public  Credential authorize(List<String> scopes, String credentialDatastore,boolean isCallback,String code) throws IOException, URISyntaxException {
        String userid="user";

        //flag to determine that the request came from the google authentication.
        if(isCallback){
            System.out.println(credentialDatastore);
            byte[] decodedStringByte;
            decodedStringByte = Base64.decodeBase64(credentialDatastore);
            GoogleRequestJSON requestJSON = new Gson().fromJson(new String(decodedStringByte), GoogleRequestJSON.class);
            userid=requestJSON.getUserid();
            credentialDatastore=requestJSON.getCredentialDatastore();

        }

        try {
            // Load client secrets.
            URI filePath = new URI(GOOGLE_APIKEY);
            Reader clientSecretReader = new InputStreamReader(new FileInputStream(filePath.toString()));

            //Reader clientSecretReader = new InputStreamReader(Auth.class.getResourceAsStream(GOOGLE_APIKEY));
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);


            // Checks that the defaults have been replaced (Default = "Enter X here").
            if (clientSecrets.getDetails().getClientId().startsWith("Enter") || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
                System.out.println(
                        "Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential "
                                + "into src/main/resources/client_secrets.json");
                System.exit(1);
            }

            // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
            FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
            DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);


            //Builds a login URL based on client ID, secret, callback URI, and scopes
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                    .Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes)
                    .setAccessType("offline")
                    .setApprovalPrompt("auto")
                    .setCredentialDataStore(datastore)
                    .build();




            InetAddress address = InetAddress.getByName("demo.omoto.io");
            System.out.println("address.getName : "+address.getHostName()+" ; address.getCanonicalHostName() : "  +address.getCanonicalHostName()+"; address.getHostAddress() : "+address.getHostAddress());

            // Build the local server with localhost and any port available.
            //used only to pass it in the ExtendedAuthorizationCodeInstalledApp
            localReceiver = new LocalServerReceiver.Builder().build();

            System.out.println(new LocalServerReceiver.Builder().setHost(address.getHostName()).setPort(8081).getHost());

            System.out.println(localReceiver);


            Credential credential=null;
            ExtendedAuthorizationCodeInstalledApp authorizationCodeInstalledApp=new ExtendedAuthorizationCodeInstalledApp(flow,localReceiver);

            //get the credential object from the stored
            credential=authorizationCodeInstalledApp.getAuthorizationFromStorage(userid);

            //if there is no credentials found ask to authenticate
            //this will redirect to the login and will wait till the user login and authorize the app
            if(credential==null)
                authorizationCodeInstalledApp.getAuthorizationFromGoogle(userid,credentialDatastore);


            if(isCallback)
                credential=authorizationCodeInstalledApp.saveAuthorizationFromGoogle(userid,credentialDatastore,code);

            // Authorize.
            //return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");

            return credential;
        }catch(Exception e){
            e.printStackTrace();
            localReceiver.stop();
        }finally{
            System.out.println("localReceiver shutting down ..");
            localReceiver.stop();
        }

        return null;
    }


    public Credential generateCredentialWithUserApprovedToken() throws IOException,
            GeneralSecurityException,URISyntaxException {
        // Load client secrets.
        URI filePath = new URI (GOOGLE_APIKEY);
        Reader clientSecretReader =new InputStreamReader(new FileInputStream(filePath.toString()));

        //Reader clientSecretReader = new InputStreamReader(Auth.class.getResourceAsStream(GOOGLE_APIKEY));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

        return null;
    }

    /**
     *
     * @param clientId
     * @param clientSecret
     * @param jsonFactory
     * @param transport
     * @param refreshToken
     * @return
     * @throws IOException
     */
    public GoogleCredential getCredentials(String clientId,String clientSecret,JsonFactory jsonFactory,HttpTransport transport,String refreshToken) throws IOException{

        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setTransport(transport)
                .setJsonFactory(jsonFactory)
                .build();

        credential.setRefreshToken(refreshToken);


        // Do a refresh so we can fail early rather than return an unusable credential
        credential.refreshToken();

        return credential;

    }


}


