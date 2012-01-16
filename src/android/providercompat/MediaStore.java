/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.providercompat;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The Media provider contains meta data for all available media on both internal
 * and external storage devices.
 */
public final class MediaStore {
    private final static String TAG = "MediaStore";

    public static final String AUTHORITY = "media";

    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";

   /**
     * Broadcast Action:  A broadcast to indicate the end of an MTP session with the host.
     * This broadcast is only sent if MTP activity has modified the media database during the
     * most recent MTP session.
     *
     * @hide
     */
    public static final String ACTION_MTP_SESSION_END = "android.provider.action.MTP_SESSION_END";

    /**
     * Activity Action: Launch a music player.
     * The activity should be able to play, browse, or manipulate music files stored on the device.
     */
    public static final String INTENT_ACTION_MUSIC_PLAYER = "android.intent.action.MUSIC_PLAYER";

    /**
     * Activity Action: Perform a search for media.
     * Contains at least the {@link android.app.SearchManager#QUERY} extra.
     * May also contain any combination of the following extras:
     * EXTRA_MEDIA_ARTIST, EXTRA_MEDIA_ALBUM, EXTRA_MEDIA_TITLE, EXTRA_MEDIA_FOCUS
     *
     * @see android.provider.MediaStore#EXTRA_MEDIA_ARTIST
     * @see android.provider.MediaStore#EXTRA_MEDIA_ALBUM
     * @see android.provider.MediaStore#EXTRA_MEDIA_TITLE
     * @see android.provider.MediaStore#EXTRA_MEDIA_FOCUS
     */
    public static final String INTENT_ACTION_MEDIA_SEARCH = "android.intent.action.MEDIA_SEARCH";

    /**
     * An intent to perform a search for music media and automatically play content from the
     * result when possible. This can be fired, for example, by the result of a voice recognition
     * command to listen to music.
     * <p>
     * Contains the {@link android.app.SearchManager#QUERY} extra, which is a string
     * that can contain any type of unstructured music search, like the name of an artist,
     * an album, a song, a genre, or any combination of these.
     * <p>
     * Because this intent includes an open-ended unstructured search string, it makes the most
     * sense for apps that can support large-scale search of music, such as services connected
     * to an online database of music which can be streamed and played on the device.
     */
    public static final String INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH =
            "android.media.action.MEDIA_PLAY_FROM_SEARCH";
    
    /**
     * The name of the Intent-extra used to define the artist
     */
    public static final String EXTRA_MEDIA_ARTIST = "android.intent.extra.artist";
    /**
     * The name of the Intent-extra used to define the album
     */
    public static final String EXTRA_MEDIA_ALBUM = "android.intent.extra.album";
    /**
     * The name of the Intent-extra used to define the song title
     */
    public static final String EXTRA_MEDIA_TITLE = "android.intent.extra.title";
    /**
     * The name of the Intent-extra used to define the search focus. The search focus
     * indicates whether the search should be for things related to the artist, album
     * or song that is identified by the other extras.
     */
    public static final String EXTRA_MEDIA_FOCUS = "android.intent.extra.focus";

    /**
     * The name of the Intent-extra used to control the orientation of a ViewImage or a MovieView.
     * This is an int property that overrides the activity's requestedOrientation.
     * @see android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
     */
    public static final String EXTRA_SCREEN_ORIENTATION = "android.intent.extra.screenOrientation";

    /**
     * The name of an Intent-extra used to control the UI of a ViewImage.
     * This is a boolean property that overrides the activity's default fullscreen state.
     */
    public static final String EXTRA_FULL_SCREEN = "android.intent.extra.fullScreen";

    /**
     * The name of an Intent-extra used to control the UI of a ViewImage.
     * This is a boolean property that specifies whether or not to show action icons.
     */
    public static final String EXTRA_SHOW_ACTION_ICONS = "android.intent.extra.showActionIcons";

    /**
     * The name of the Intent-extra used to control the onCompletion behavior of a MovieView.
     * This is a boolean property that specifies whether or not to finish the MovieView activity
     * when the movie completes playing. The default value is true, which means to automatically
     * exit the movie player activity when the movie completes playing.
     */
    public static final String EXTRA_FINISH_ON_COMPLETION = "android.intent.extra.finishOnCompletion";

    /**
     * The name of the Intent action used to launch a camera in still image mode.
     */
    public static final String INTENT_ACTION_STILL_IMAGE_CAMERA = "android.media.action.STILL_IMAGE_CAMERA";

    /**
     * The name of the Intent action used to launch a camera in video mode.
     */
    public static final String INTENT_ACTION_VIDEO_CAMERA = "android.media.action.VIDEO_CAMERA";

    /**
     * Standard Intent action that can be sent to have the camera application
     * capture an image and return it.
     * <p>
     * The caller may pass an extra EXTRA_OUTPUT to control where this image will be written.
     * If the EXTRA_OUTPUT is not present, then a small sized image is returned as a Bitmap
     * object in the extra field. This is useful for applications that only need a small image.
     * If the EXTRA_OUTPUT is present, then the full-sized image will be written to the Uri
     * value of EXTRA_OUTPUT.
     * @see #EXTRA_OUTPUT
     */
    public final static String ACTION_IMAGE_CAPTURE = "android.media.action.IMAGE_CAPTURE";

    /**
     * Standard Intent action that can be sent to have the camera application
     * capture an video and return it.
     * <p>
     * The caller may pass in an extra EXTRA_VIDEO_QUALITY to control the video quality.
     * <p>
     * The caller may pass in an extra EXTRA_OUTPUT to control
     * where the video is written. If EXTRA_OUTPUT is not present the video will be
     * written to the standard location for videos, and the Uri of that location will be
     * returned in the data field of the Uri.
     * @see #EXTRA_OUTPUT
     * @see #EXTRA_VIDEO_QUALITY
     * @see #EXTRA_SIZE_LIMIT
     * @see #EXTRA_DURATION_LIMIT
     */
    public final static String ACTION_VIDEO_CAPTURE = "android.media.action.VIDEO_CAPTURE";

    /**
     * The name of the Intent-extra used to control the quality of a recorded video. This is an
     * integer property. Currently value 0 means low quality, suitable for MMS messages, and
     * value 1 means high quality. In the future other quality levels may be added.
     */
    public final static String EXTRA_VIDEO_QUALITY = "android.intent.extra.videoQuality";

    /**
     * Specify the maximum allowed size.
     */
    public final static String EXTRA_SIZE_LIMIT = "android.intent.extra.sizeLimit";

    /**
     * Specify the maximum allowed recording duration in seconds.
     */
    public final static String EXTRA_DURATION_LIMIT = "android.intent.extra.durationLimit";

    /**
     * The name of the Intent-extra used to indicate a content resolver Uri to be used to
     * store the requested image or video.
     */
    public final static String EXTRA_OUTPUT = "output";

    /**
      * The string that is used when a media attribute is not known. For example,
      * if an audio file does not have any meta data, the artist and album columns
      * will be set to this value.
      */
    public static final String UNKNOWN_STRING = "<unknown>";

    /**
     * Common fields for most MediaProvider tables
     */

    public interface MediaColumns extends BaseColumns {
        /**
         * The data stream for the file
         * <P>Type: DATA STREAM</P>
         */
        public static final String DATA = "_data";

        /**
         * The size of the file in bytes
         * <P>Type: INTEGER (long)</P>
         */
        public static final String SIZE = "_size";

        /**
         * The display name of the file
         * <P>Type: TEXT</P>
         */
        public static final String DISPLAY_NAME = "_display_name";

        /**
         * The title of the content
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";

        /**
         * The time the file was added to the media provider
         * Units are seconds since 1970.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DATE_ADDED = "date_added";

        /**
         * The time the file was last modified
         * Units are seconds since 1970.
         * NOTE: This is for internal use by the media scanner.  Do not modify this field.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DATE_MODIFIED = "date_modified";

        /**
         * The MIME type of the file
         * <P>Type: TEXT</P>
         */
        public static final String MIME_TYPE = "mime_type";

        /**
         * The MTP object handle of a newly transfered file.
         * Used to pass the new file's object handle through the media scanner
         * from MTP to the media provider
         * For internal use only by MTP, media scanner and media provider.
         * <P>Type: INTEGER</P>
         * @hide
         */
        public static final String MEDIA_SCANNER_NEW_OBJECT_ID = "media_scanner_new_object_id";

        /**
         * Non-zero if the media file is drm-protected
         * <P>Type: INTEGER (boolean)</P>
         * @hide
         */
        public static final String IS_DRM = "is_drm";

        /**
         * The width of the image/video in pixels.
         * @hide
         */
        public static final String WIDTH = "width";

        /**
         * The height of the image/video in pixels.
         * @hide
         */
        public static final String HEIGHT = "height";
     }

    /**
     * Media provider table containing an index of all files in the media storage,
     * including non-media files.  This should be used by applications that work with
     * non-media file types (text, HTML, PDF, etc) as well as applications that need to
     * work with multiple media file types in a single query.
     */
    public static final class Files {

        /**
         * Get the content:// style URI for the files table on the
         * given volume.
         *
         * @param volumeName the name of the volume to get the URI for
         * @return the URI to the files table on the given volume
         */
        public static Uri getContentUri(String volumeName) {
            return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName +
                    "/file");
        }

        /**
         * Get the content:// style URI for a single row in the files table on the
         * given volume.
         *
         * @param volumeName the name of the volume to get the URI for
         * @param rowId the file to get the URI for
         * @return the URI to the files table on the given volume
         */
        public static final Uri getContentUri(String volumeName,
                long rowId) {
            return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName
                    + "/file/" + rowId);
        }

        /**
         * For use only by the MTP implementation.
         * @hide
         */
        public static Uri getMtpObjectsUri(String volumeName) {
            return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName +
                    "/object");
        }

        /**
         * For use only by the MTP implementation.
         * @hide
         */
        public static final Uri getMtpObjectsUri(String volumeName,
                long fileId) {
            return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName
                    + "/object/" + fileId);
        }

        /**
         * Used to implement the MTP GetObjectReferences and SetObjectReferences commands.
         * @hide
         */
        public static final Uri getMtpReferencesUri(String volumeName,
                long fileId) {
            return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName
                    + "/object/" + fileId + "/references");
        }

        /**
         * Fields for master table for all media files.
         * Table also contains MediaColumns._ID, DATA, SIZE and DATE_MODIFIED.
         */
        public interface FileColumns extends MediaColumns {
            /**
             * The MTP storage ID of the file
             * <P>Type: INTEGER</P>
             * @hide
             */
            public static final String STORAGE_ID = "storage_id";

            /**
             * The MTP format code of the file
             * <P>Type: INTEGER</P>
             * @hide
             */
            public static final String FORMAT = "format";

            /**
             * The index of the parent directory of the file
             * <P>Type: INTEGER</P>
             */
            public static final String PARENT = "parent";

            /**
             * The MIME type of the file
             * <P>Type: TEXT</P>
             */
            public static final String MIME_TYPE = "mime_type";

            /**
             * The title of the content
             * <P>Type: TEXT</P>
             */
            public static final String TITLE = "title";

            /**
             * The media type (audio, video, image or playlist)
             * of the file, or 0 for not a media file
             * <P>Type: TEXT</P>
             */
            public static final String MEDIA_TYPE = "media_type";

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file
             * is not an audio, image, video or playlist file.
             */
            public static final int MEDIA_TYPE_NONE = 0;

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file is an image file.
             */
            public static final int MEDIA_TYPE_IMAGE = 1;

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file is an audio file.
             */
            public static final int MEDIA_TYPE_AUDIO = 2;

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file is an video file.
             */
            public static final int MEDIA_TYPE_VIDEO = 3;

            /**
             * Constant for the {@link #MEDIA_TYPE} column indicating that file is an playlist file.
             */
            public static final int MEDIA_TYPE_PLAYLIST = 4;
        }
    }

}
