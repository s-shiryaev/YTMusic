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
 *<p>Usage example with callback:
 *<pre>
 *<code>private ExtractCallBackInterface mCall = new ExtractCallBackInterface() {
 *         {@literal @}Override
 *         public void onSuccExtract(String url, String parsedUrl, String title) {
 *             //do something
 *         }</code></pre>
 *<p>...
 *<p>{@code new YTExtract(this, mCall).extract(url)}
 *<p>Usage example without callback:
 * <pre>
 * <code>new YTExtract(this){
 *            {@literal @}Override
 *             protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
 *                  int itag = 140; //sound
 *                  if (ytFiles!=null){
 *                      String parsedUrl = ytFiles.get(itag).getUrl();
 *                      //do something
 *                  }
 *             }
 *          }.extract(url);</code></pre>
 *
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
     */
    public void extract(String videoUrl) {
        this.videoUrl = videoUrl;
        super.extract(videoUrl, false, true);
    }

    /**
     * <p>Same as {@link #extract(String videoUrl)} but the tasks caused by this method
     * will be executed in parallel.
     * <p>Use it if you need to extract the link as quickly as possible.
     * <p>All tasks caused by this method will be executed in parallel in separate threads,
     * if possible. However, there is a limit on the maximum number of threads. If the limit
     * is reached, task queues will be created for each thread.
     *
     * @param videoUrl  Youtube video URL. Works with mobile links too.
     */
    public void extract_now(String videoUrl) {
        ((YouTubeExtractor) this).setIncludeWebM(true);
        ((YouTubeExtractor) this).setParseDashManifest(false);
        this.videoUrl=videoUrl;
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, videoUrl);
    }

    /**
     * Standard extract method with custom parameters.
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
     */
    public void extract(String videoUrl, boolean parseDashManifest, boolean includeWebM) {
        this.parseDashManifest = parseDashManifest;
        this.includeWebM = includeWebM;
        this.videoUrl=videoUrl;
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
     */
    public void extract_now(String videoUrl, boolean includeWebM, boolean parseDashManifest){
        ((YouTubeExtractor) this).setIncludeWebM(includeWebM);
        ((YouTubeExtractor) this).setParseDashManifest(parseDashManifest);
        this.parseDashManifest = parseDashManifest;
        this.includeWebM = includeWebM;
        this.videoUrl=videoUrl;
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, videoUrl);
    }

    @Override
    protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
        int itag = 140;
        if (ytFiles!=null){
            String parsedUrl = ytFiles.get(itag).getUrl();
            if (callbackReference != null) {
                callbackReference.get().onSuccExtract(videoUrl,parsedUrl,vMeta.getTitle());
            }
        } else {
            if (callbackReference != null) {
                callbackReference.get().onUnsuccExtractTryAgain(attempt);
            }
            if (attempt<maxAttempts){
                if (callbackReference != null) {
                    new YTExtract(con.get(), attempt + 1, maxAttempts,
                            callbackReference.get())
                            .extract_now(videoUrl, parseDashManifest, includeWebM);
                } else {
                    new YTExtract(con.get(), attempt + 1, maxAttempts)
                            .extract_now(videoUrl, parseDashManifest, includeWebM);
                }
            } else {
                if (callbackReference != null) {
                    callbackReference.get().onUnsuccExtract(videoUrl);
                }
            }
        }
    }

    public interface ExtractCallBackInterface {
        void onSuccExtract(String url, String parsedUrl, String title);
        void onUnsuccExtractTryAgain(int attempt);
        void onUnsuccExtract(String url);
    }
}
