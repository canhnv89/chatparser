package com.web2mine.chatpaser.parser;

import android.content.Context;

import com.web2mine.chatpaser.common.DEBUG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Universal parser which manages all implemented parsers and build the final parsed result
 */
public class UniParser {

    /**
     * The callback to transfer result to requester
     */
    public interface OnParsingFinishListener {
        /**
         * Return parsed result JSON text
         *
         * @param jsonObject  JSON object of parsed result
         * @param requestCode The request code from requester to assign the result to its position in the request list
         */
        void OnParsingFinished(JSONObject jsonObject, int requestCode);
    }

    /**
     * A bundle of parser and its JSON result. This bundle is necessary because the parsing task is asynchronous
     * parser can complete its task at any time. This bundle is map the result to its parser
     */
    private static class ParseResultItem {
        ItemParser Parser;
        JSONArray JsonResult;

        public ParseResultItem(ItemParser parser, JSONArray jsonResult) {
            Parser = parser;
            JsonResult = jsonResult;
        }
    }

    /**
     * UniParsingTask maps the list of results from all parser to requestId and counting the done parser before returning results to requester
     */
    private static class UniParsingTask {
        int RequestId;
        int DoneCounter = 0;//Done parser counter
        List<ParseResultItem> Results = new ArrayList<>();//List of done parsers and parsed results

        public UniParsingTask(int requestId) {
            RequestId = requestId;
        }
    }

    //List of all parsers (mention, emoticon, links...)
    private List<ItemParser> mParserList = new ArrayList<>();

    //Mapping the task id to it processing task because processing task is asynchronous
    private Map<Integer, UniParsingTask> mTaskMap = new HashMap<>();

    private static int mTaskID = 0;
    //Parsing result listener. Keep its inside a week reference to avoid any leaks due to its reference
    private WeakReference<OnParsingFinishListener> mOnParsingFinishListener;

    //Callback listener for each parser which it complete its task.
    private ItemParser.OnResultListener mOnResultListener = new ItemParser.OnResultListener() {
        @Override
        public void onDone(int taskId, JSONArray jsonObject, ItemParser itemParser) {
            //We got the result when a parser complete the assigned task
            UniParsingTask task = mTaskMap.get(taskId);//Find the universal parsing task (of this result
            if (task == null) {
                DEBUG.error("task==null. Something wrong!");
                return;
            }
            task.DoneCounter++; //Increase the counter of done parser
            if (jsonObject != null)
                task.Results.add(new ParseResultItem(itemParser, jsonObject)); //Store result if it's valid

            //If all parsers are done
            if (task.DoneCounter == mParserList.size()) {
                //Combine the results of all parsers and notify the requester
                onTaskDone(taskId);
            }
        }
    };

    /**
     * Construct a universal parser instance
     *
     * @param listener Result callback listener
     */
    public UniParser(Context context, OnParsingFinishListener listener) {
        //Store the callback listener
        mOnParsingFinishListener = new WeakReference<OnParsingFinishListener>(listener);

        //Create instance of mention parser
        ItemParser mentionParser = new MentionParser(context);
        mentionParser.setOnResultListener(mOnResultListener);
        mParserList.add(mentionParser);

        //Create instance of emoticon parser
        ItemParser emoticon = new EmoticonParser(context);
        emoticon.setOnResultListener(mOnResultListener);
        mParserList.add(emoticon);

        //Create instance of link parser
        ItemParser linkParser = new LinkParser(context);
        linkParser.setOnResultListener(mOnResultListener);
        mParserList.add(linkParser);

        //Add more parser here if function extension is required.
        //......
        //......
    }

    /**
     * Combine results into a single JSON Object and send it to requester
     *
     * @param taskId Task ID of UniParsingTask
     */
    private void onTaskDone(int taskId) {
        UniParsingTask task = mTaskMap.get(taskId); //Retrieve UniParsingTask of this task id
        if (task == null) //Oops, something wrong here. How comes?
        {
            DEBUG.error("task==null. Something wrong!");
            return;
        }
        //A combined JSON object of results
        JSONObject uniJsonObject = new JSONObject();
        //Add each result to the combined JSON object
        for (ParseResultItem result : task.Results) {
            String type = result.Parser.getType();
            try {
                uniJsonObject.put(type, result.JsonResult);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Send it to requester
        if (mOnParsingFinishListener.get() != null)
            mOnParsingFinishListener.get().OnParsingFinished(uniJsonObject, task.RequestId);

        //Remove task from task map to save memory
        task.Results.clear();
        mTaskMap.remove(taskId);
    }

    /**
     * Requester call this to execute the task
     *
     * @param input     Original chat text
     * @param requestId Request id from requester
     */
    public void parser(String input, int requestId) {
        mTaskID++;//Increase the internal task id
        //if it's too big, reset it
        if (mTaskID >= 10000) mTaskID = 0;
        //Create a parsing task and keep it in the map a long with task id
        mTaskMap.put(mTaskID, new UniParsingTask(requestId));

        //Request each parser to carry out the task
        for (ItemParser parser : mParserList) {
            parser.parse(mTaskID, input);
        }
    }

    //Clean resources
    public void destroy() {
        for (ItemParser parser : mParserList) {
            parser.destroy();
        }
        mTaskMap.clear();
        mParserList.clear();
    }
}
