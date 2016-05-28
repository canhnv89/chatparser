package com.web2mine.chatpaser.parser;

import android.content.Context;
import android.os.Handler;

import com.web2mine.chatpaser.common.DEBUG;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Root parser class of chat text. It provide parsing function upon the pattern defined in the child class (extending class).
 * Moreover, each child class can customize the parsed results upon its purpose. When an additional parser is needed,
 * just simply create a new child class of this
 */
public abstract class ItemParser {

    //We use max. 10 threads to process parsing request
    static final int MAX_EXECUTOR_THREADS = 10;

    //Handler to post parsed results to UI thread
    private Handler mUIThreadHandler;

    //Executor to process parsing in the background threads
    private ExecutorService mExecutorService;

    //Listener to receive callback after parsing is done
    protected OnResultListener mOnResultListener;

    //Get parsing pattern from child class
    protected abstract Pattern getPattern();

    //Get type of parsing information (mention, emoticon, link...)
    public abstract String getType();

    //Parsing callback interface to receive parsed results
    public interface OnResultListener {
        /**
         * Return parsed result when parsing is done
         * @param taskId To support multi-processing, this parameter is needed to indicate the owner of results
         * @param jsonObject JSON Array containing parsed results
         * @param itemParser Parser which processed the task
         */
        void onDone(int taskId, JSONArray jsonObject, ItemParser itemParser);
    }

    /**
     * Construction function
     * @param context Context to create handler in UI thread
     */
    public ItemParser(Context context)
    {
        //Create handler in ui thread
        mUIThreadHandler = new Handler(context.getMainLooper());
        //Setup executor service
        mExecutorService = Executors.newFixedThreadPool(MAX_EXECUTOR_THREADS);
    }

    /**
     * Call this to stop background service
     */
    public void destroy()
    {
        mExecutorService.shutdownNow();
    }

    /**
     * Set a callback for receiving parsing result
     * @param listener Result callback listener
     */
    public void setOnResultListener(OnResultListener listener) {
        mOnResultListener = listener;
    }

    /**
     * Parsing a text in the background task
     * @param taskId  owner
     * @param input Text need to be parsed
     */
    public void parse(final int taskId,final String input) {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //Check validity of input params
                if (input==null||input.isEmpty()) {
                    DEBUG.error("No input!");
                    return;
                }
                //Create a list to receive results
                ArrayList<String> list = new ArrayList<>();
                //Find any text matched the pattern
                Matcher matcher = getPattern().matcher(input);
                //Obtain all matched text
                while (matcher.find()) {
                    String item = matcher.group();
                    list.add(item);
                }
                //Build the JSON Array result from obtained list
                final JSONArray jsonResult = buildJson(list);
                mUIThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Send callback for results. If no callback, do nothing
                        if (mOnResultListener == null) {
                            DEBUG.error("No listener to receive result");
                            return;
                        }
                        //Transfer results via callback
                        mOnResultListener.onDone(taskId, jsonResult, ItemParser.this);
                    }
                });
            }
        });
    }

    /**
     * Build JSON Array from parsed strings
     * @param list Parsed result list
     * @return JSON Array of results
     */
    protected JSONArray buildJson(ArrayList<String> list) {
        //If list is empty, nothing to do
        if (list == null || list.size() == 0) {
            DEBUG.show("No item of " + getType() + " found");
            return null;
        }

        //Build JSON Array
        JSONArray jsonArray = new JSONArray();
        for (String item : list) {
            jsonArray.put(item);
        }

        //Done, return it
        return jsonArray;
    }
}
