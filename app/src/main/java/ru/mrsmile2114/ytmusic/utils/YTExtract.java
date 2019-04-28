package ru.mrsmile2114.ytmusic.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;


/**
 *<h1>YTExtract</h1>
 *<p>Child class of {@link at.huber.youtubeExtractor.YouTubeExtractor}.
 *<p>Implements attempts to get a link after a failure and can work with callbacks.
 *<p>
 *<p>Usage example:
 *<pre>
 *<code>private ExtractCallBackInterface mCall = new ExtractCallBackInterface() {
 *         {@literal @}Override
 *         public void onSuccExtract(String url, String parsedUrl, String title) {
 *             //do something
 *         }
 *         ...</code></pre>
 *<p>...
 *<p>{@code new YTExtract(this, mCall).extract(url, itags)}
 *
 * @author Sergey Shiryaev
 **/
 public class YTExtract extends YouTubeExtractor {

    private static int MAX_ATTEMPTS = 6;

    private WeakReference<Context> con;
    private int attempt;
    private int maxAttempts;
    private String videoUrl;
    private WeakReference<ExtractCallBackInterface> callbackReference;
    private boolean parseDashManifest;
    private boolean includeWebM;
    private int[] itags;

    /**
     *<p>Standard {@link YTExtract} constructor. Use it if you do not want to override method
     *{@link #onExtractionComplete(SparseArray, VideoMeta)}.</p>
     *<p><b>If you use this constructor, you should create callback
     *{@link ExtractCallBackInterface}</b></p>
     *
     * @param con       The context where the link will be extracted.
     * @param callBack  Instance of callback ({@link ExtractCallBackInterface})
     */
    public YTExtract(@NonNull Context con, ExtractCallBackInterface callBack){
        super(con);
        this.con=new WeakReference<>(con);
        this.attempt=1;
        this.maxAttempts=MAX_ATTEMPTS;
        this.callbackReference=new WeakReference<>(callBack);
    }

    /**
     *<p>Standard {@link YTExtract} constructor.</p>
     *<p><b>If you use this constructor, you should override method
     *{@link #onExtractionComplete(SparseArray, VideoMeta)}</b></p>
     *
     * @deprecated It is not recommended to use, because non-static AsyncTask classes can
     * cause memory leaks. Use {@link #YTExtract(Context, ExtractCallBackInterface)}
     *
     * @param con The context where the link will be extracted.
     **/
    public YTExtract (@NonNull Context con){
        super(con);
        this.con=new WeakReference<>(con);
        this.attempt=1;
        this.maxAttempts=MAX_ATTEMPTS;
    }


    private YTExtract(@NonNull Context con, int attempt, int maxAttempts,
                     ExtractCallBackInterface callBack) {
        super(con);
        this.con=new WeakReference<>(con);
        this.attempt=attempt;
        this.maxAttempts=maxAttempts;
        this.callbackReference=new WeakReference<>(callBack);
    }

    private YTExtract(@NonNull Context con, int attempt, int maxAttempts) {
        super(con);
        this.con=new WeakReference<>(con);
        this.attempt=attempt;
        this.maxAttempts=maxAttempts;
    }

    /**
     * Standard extract method with default parameters.
     * All tasks caused by this method are executed in the same thread successively.
     *
     * @param videoUrl  Youtube video URL. Works with mobile links too.
     * @param itags     YouTube video stream format codes. E.g.  video 720p mp4  - 22,
     *                  audio 128k - 140. See all formats
     *                  <a href="https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2">
     *                  here</a>.You can get several versions of a file at once (a callback
     *                  will be called for each one) like this: {@code 140, 141, 22}
     */
    public void extract(String videoUrl, int... itags) {
        this.videoUrl = videoUrl;
        this.itags = itags;
        super.extract(videoUrl, false, true);
    }

    /**
     * <p>Same as {@link #extract(String videoUrl, int... itags)} but the tasks caused
     * by this method will be executed in parallel.
     * <p>Use it if you need to extract the link as quickly as possible.
     * <p>All tasks caused by this method will be executed in parallel in separate threads,
     * if possible. However, there is a limit on the maximum number of threads. If the limit
     * is reached, task queues will be created for each thread.
     *
     * @param videoUrl  Youtube video URL. Works with mobile links too.
     * @param itags     YouTube video stream format codes. E.g.  video 720p mp4  - 22,
     *                  audio 128k - 140. See all formats
     *                  <a href="https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2">
     *                  here</a>.You can get several versions of a file at once (a callback
     *                  will be called for each one) like this: {@code 140, 141, 22}
     */
    public void extract_now(String videoUrl, int... itags) {
        ((YouTubeExtractor) this).setIncludeWebM(true);
        ((YouTubeExtractor) this).setParseDashManifest(false);
        this.videoUrl=videoUrl;
        this.itags = itags;
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, videoUrl);
    }

    /**
     * Standard extract method with custom parameters.
     *
     * All tasks caused by this method are executed in the same thread successively.
     *
     * @param videoUrl          Youtube video URL. Works with mobile links too.
     * @param parseDashManifest The dash manifest contains dash streams and usually additionally
     *                          the higher quality audio formats. But the main difference is that
     *                          dash streams from the dash manifest seem to not get throttled by
     *                          the YouTube servers. If you don't use the dash streams at all leave
     *                          it deactivated since it needs to download additional files for
     *                          extraction.
     * @param includeWebM       If set to false it excludes the webm container format streams
     *                          from the result.
     * @param itags     YouTube video stream format codes. E.g.  video 720p mp4  - 22,
     *                  audio 128k - 140. See all formats
     *                  <a href="https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2">
     *                  here</a>.You can get several versions of a file at once (a callback
     *                  will be called for each one) like this: {@code 140, 141, 22}
     */
    public void extract(String videoUrl, boolean parseDashManifest, boolean includeWebM,
                        int... itags) {
        this.parseDashManifest = parseDashManifest;
        this.includeWebM = includeWebM;
        this.videoUrl=videoUrl;
        this.itags=itags;
        super.extract(videoUrl, parseDashManifest, includeWebM);
    }

    /**
     * <p>Same as {@link #extract(String videoUrl, boolean, boolean)}but the tasks caused by
     * this method will be executed in parallel.
     * <p>All tasks caused by this method will be executed in parallel in separate threads,
     * if possible. However, there is a limit on the maximum number of threads. If the limit
     * is reached, task queues will be created for each thread.
     *
     * @param videoUrl          Youtube video URL. Works with mobile links too.
     * @param parseDashManifest The dash manifest contains dash streams and usually additionally
     *                          the higher quality audio formats. But the main difference is that
     *                          dash streams from the dash manifest seem to not get throttled by
     *                          the YouTube servers. If you don't use the dash streams at all leave
     *                          it deactivated since it needs to download additional files for
     *                          extraction.
     * @param includeWebM       If set to false it excludes the webm container format streams
     *                          from the result.
     * @param itags     YouTube video stream format codes. E.g.  video 720p mp4  - 22,
     *                  audio 128k - 140. See all formats
     *                  <a href="https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2">
     *                  here</a>.You can get several versions of a file at once (a callback
     *                  will be called for each one) like this: {@code 140, 141, 22}
     */
    public void extract_now(String videoUrl, boolean includeWebM,
                            boolean parseDashManifest, int... itags){
        ((YouTubeExtractor) this).setIncludeWebM(includeWebM);
        ((YouTubeExtractor) this).setParseDashManifest(parseDashManifest);
        this.parseDashManifest = parseDashManifest;
        this.includeWebM = includeWebM;
        this.videoUrl=videoUrl;
        this.itags=itags;
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, videoUrl);
    }

    @Override
    protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
        if (ytFiles!=null){
            for(int i=0;i<itags.length;i++){
                String parsedUrl = ytFiles.get(itags[i]).getUrl();
                if (callbackReference != null) {
                    callbackReference.get().onSuccExtract(videoUrl,parsedUrl,vMeta.getTitle());
                }
            }
        } else {
            if (callbackReference != null) {
                callbackReference.get().onUnsuccExtractTryAgain(attempt);
            }
            if (attempt<maxAttempts){
                if (callbackReference != null) {
                    new YTExtract(con.get(), attempt + 1, maxAttempts,
                            callbackReference.get())
                            .extract_now(videoUrl, parseDashManifest, includeWebM, itags);
                } else {
                    new YTExtract(con.get(), attempt + 1, maxAttempts)
                            .extract_now(videoUrl, parseDashManifest, includeWebM, itags);
                }
            } else {
                if (callbackReference != null) {
                    callbackReference.get().onUnsuccExtract(videoUrl);
                }
            }
        }
    }

    /**
     *  A callback interface whose methods are called depending on the result of
     *  extracting the link.
     */
    public interface ExtractCallBackInterface {
        /**
         * Called when the link is successfully extracted.
         * @param url       Original video URL.
         * @param parsedUrl URL to the file that we extracted.
         * @param title     Video title.
         */
        void onSuccExtract(String url, String parsedUrl, String title);

        /**
         * Called if an error occurred while receiving a link and an attempt is made
         * to extract the link again.
         * @param attempt   Attempt number
         */
        void onUnsuccExtractTryAgain(int attempt);

        /**
         * Called if all attempts to extract the link were unsuccessful.
         * @param url   Original video URL.
         */
        void onUnsuccExtract(String url);
    }
}
