package com.socialmedia.google.youtube.youtubeHelper;

/**
 * Created by omoto on 24/8/16.
 */
/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

import com.google.common.collect.Lists;

import com.socialmedia.SocialMediaRequestJSON;
import com.socialmedia.google.Auth;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Upload a video to the authenticated user's channel. Use OAuth 2.0 to
 * authorize the request. Note that you must add your video files to the
 * project folder to upload them with this application.
 *
 * @author Jeremy Walker
 */
public class UploadVideoHelper {

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;

    /**
     * Define a global variable that specifies the MIME type of the video
     * being uploaded.
     */
    private static final String VIDEO_FILE_FORMAT = "video/*";

    private static final String SAMPLE_VIDEO_FILENAME = "/sample-video.mp4";

    private static final String SAMPLE_VIDEO_PATH = "/home/azureuser";

    //the information of the video that is returned
    Video returnedVideo = null;


    /**
     * Upload the user-selected video to the user's YouTube channel. The code
     * looks for the video in the application's project folder and uses OAuth
     * 2.0 to authorize the API request.
     *
     * @param socialMediaRequestJSON name of the video filename that is to be uploaded in Youtube
     */
//    public static void main(String[] args) {
    public Object uploadVideo(SocialMediaRequestJSON socialMediaRequestJSON) {
        boolean isCallback = false;
        Credential credential;
        String code = "";

        // This OAuth 2.0 access scope allows an application to upload files
        // to the authenticated user's YouTube channel, but doesn't allow
        // other types of access.
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");

        //extracting the code from the socialmediarequestJSON this will
        //determine that the quest came from google redirect.
        if (socialMediaRequestJSON != null && socialMediaRequestJSON.getCode() != "") {
            System.out.println("client is base 64 encoded....");
            code = socialMediaRequestJSON.getCode();
            //setting the callback flag on
            isCallback = true;
            try {
                credential = new Auth().authorize(scopes, socialMediaRequestJSON.getUserid(), isCallback, code);
                System.out.println("User credential saved . Thanks " + socialMediaRequestJSON.getCredentialDatastore() + " ! ");
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            /* normal call to upload videos */
            try {
                // Authorize the request.
                //Credential credential = new Auth().authorize(scopes, "uploadvideo");
                System.out.println("uploading video in Youtube.");

                credential = new Auth().authorize(scopes, socialMediaRequestJSON.getUserid(), isCallback, code);


                if (credential != null) {
                    // This object is used to make YouTube Data API requests.
                    youtube = new YouTube
                            .Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential)
                            .setApplicationName("omoto youtube upload testing")
                            .build();


                    System.out.println("Uploading: " + socialMediaRequestJSON.getVideoPath());

                    // Add extra information to the video before uploading.
                    Video videoObjectDefiningMetadata = new Video();

                    // Set the video to be publicly visible. This is the default
                    // setting. Other supporting settings are "unlisted" and "private."
                    VideoStatus status = new VideoStatus();
                    status.setPrivacyStatus("public");
                    videoObjectDefiningMetadata.setStatus(status);

                    // Most of the video's metadata is set on the VideoSnippet object.
                    VideoSnippet snippet = new VideoSnippet();

                    // This code uses a Calendar instance to create a unique name and
                    // description for test purposes so that you can easily upload
                    // multiple files. You should remove this code from your project
                    // and use your own standard names instead.
                    Calendar cal = Calendar.getInstance();
                    snippet.setTitle("Test Upload via Java on " + cal.getTime());
                    snippet.setDescription(
                            "Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());

                    // Set the keyword tags that you want to associate with the video.
                    List<String> tags = new ArrayList<String>();
                    tags.add("test");
                    tags.add("example");
                    tags.add("java");
                    tags.add("YouTube Data API V3");
                    tags.add("erase me");
                    snippet.setTags(tags);

                    // Add the completed snippet object to the video resource.
                    videoObjectDefiningMetadata.setSnippet(snippet);

//            InputStream inputStream=new FileInputStream(uploadVideoName);
                    InputStream inputStream = new FileInputStream(socialMediaRequestJSON.getVideoPath());

//            InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT,UploadVideoHelper.class.getResourceAsStream(SAMPLE_VIDEO_FILENAME));
                    InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, inputStream);

                    // Insert the video. The command sends three arguments. The first
                    // specifies which information the API request is setting and which
                    // information the API response should return. The second argument
                    // is the video resource that contains metadata about the new video.
                    // The third argument is the actual video content.
                    YouTube.Videos.Insert videoInsert = youtube.videos().insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);

                    // Set the upload type and add an event listener.
                    MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

                    // Indicate whether direct media upload is enabled. A value of
                    // "True" indicates that direct media upload is enabled and that
                    // the entire media content will be uploaded in a single request.
                    // A value of "False," which is the default, indicates that the
                    // request will use the resumable media upload protocol, which
                    // supports the ability to resume an upload operation after a
                    // network interruption or other transmission failure, saving
                    // time and bandwidth in the event of network failures.
                    uploader.setDirectUploadEnabled(false);

                    MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                        public void progressChanged(MediaHttpUploader uploader) throws IOException {
                            switch (uploader.getUploadState()) {
                                case INITIATION_STARTED:
                                    System.out.println("Initiation Started");
                                    break;
                                case INITIATION_COMPLETE:
                                    System.out.println("Initiation Completed");
                                    break;
                                case MEDIA_IN_PROGRESS:
                                    System.out.println("Upload in progress");
                                    System.out.println("Upload percentage: " + uploader.getProgress());
                                    break;
                                case MEDIA_COMPLETE:
                                    System.out.println("Upload Completed!");
                                    break;
                                case NOT_STARTED:
                                    System.out.println("Upload Not Started!");
                                    break;
                            }
                        }
                    };
//            uploader.setProgressListener(progressListener);

                    uploader.getNumBytesUploaded();

                    // Call the API and upload the video.
                    returnedVideo = videoInsert.execute();

                    // Print data about the newly inserted video from the API response.
                    System.out.println("\n================== Returned Video ==================\n");
                    System.out.println("  - Id: " + returnedVideo.getId());
                    System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
                    System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
                    System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
                    System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());
                } else {

                    System.out.println("Credential object null. ....");
                }

            } catch (GoogleJsonResponseException e) {
                System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("IOException: " + e.getMessage());
                e.printStackTrace();
            } catch (Throwable t) {
                System.err.println("Throwable: " + t.getMessage());
                t.printStackTrace();
            }
        }

        return returnedVideo;
    }

    //simplest upload for youtube
    public void uploadVideo() {

    }


}
