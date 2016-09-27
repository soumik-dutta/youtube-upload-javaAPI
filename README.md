Youtube Upload using Google Java API 
===================
The idea is to upload  **Youtube** videos in the users account after authenticating the app.The authentication process will happen only once and the app can be able to upload using the stored user credentials.

The sample code for **Youtube Authentication **    
```java
public static Credential authorize(List<String> scopes, String credentialDatastore) throws IOException {

        // Load client secrets.
        Reader clientSecretReader = new InputStreamReader(Auth.class.getResourceAsStream("/client_secrets.json"));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

        // Checks that the defaults have been replaced (Default = "Enter X here").
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential "
                            + "into src/main/resources/client_secrets.json");
            System.exit(1);
        }

        // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
        DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes).setCredentialDataStore(datastore)
                .build();

        // Build the local server and bind it to port 8080
        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

        // Authorize.
        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
    }
```

Problem which I faced was 

	 1. An instance of  **jetty server** instance  which will be listening constantly until the response       is coming from Google as mentioned in the redirect url.
	 2. Though there is a function called `setHost()` inside `new LocalServerReceiver.Builder()` class      which responsible for creating a local jetty server instance, was throughing a **Cannot assign       requested address** error everytime a host name was given irrespective of the port which did not      matter.
 
	 

The solution was to stop all the dependencides from the local jetty server at the first place.But then there should a mechanism that will save the credentials when the google callback happens.This can be overcome by creating an endpoint which will save get the **authentication code** and exchanging with google for the refresh and access token.
All this functions was already given in the AuthorizationCodeInstalledApp.authorize() function.

```java
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
```

>  **authorize()** : This will check whether the credentials of the user
> is already present then it will return the user credentials.Othewise 
> following process is followed
> 
> 1.Create an url that will ask the user to give access to the app .
> 2.After successful authentication a code will be received (An instance of jetty server continuously listens untill the code is received ).
> 3.Exchange the code just received with the accesstoken and refreshtoken for offline upload.
> 4.Store the credentials that we just received from google.

----------

To customize this flow we should first create a class which will extend **AuthorizationCodeInstalledApp** class and will have all the functionality of the above function broken to serve each functionality individually.

```java 
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
```

>Here **getAuthorizationFromStorage** gets the user credentials with the userid  and returns null if it is not found.


```java
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
```

>Now in this function a custom variable is been passed along with the the redirecting URI .The custom parameters have to be encoded with Base64 format and assign it to **state** variable. This **state** variable is sent back after authentication which will identify the user for which the code is sent. 


Finally save the credentials and get the the Credential object.

```java
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
```









  [1]: http://math.stackexchange.com/
  [2]: http://daringfireball.net/projects/markdown/syntax "Markdown"
  [3]: https://github.com/jmcmanus/pagedown-extra "Pagedown Extra"
  [4]: http://meta.math.stackexchange.com/questions/5020/mathjax-basic-tutorial-and-quick-reference
  [5]: https://code.google.com/p/google-code-prettify/
  [6]: http://highlightjs.org/
  [7]: http://bramp.github.io/js-sequence-diagrams/
  [8]: http://adrai.github.io/flowchart.js/
