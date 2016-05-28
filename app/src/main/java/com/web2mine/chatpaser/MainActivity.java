package com.web2mine.chatpaser;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.web2mine.chatpaser.common.Network;
import com.web2mine.chatpaser.parser.UniParser;
import com.web2mine.chatpaser.common.DEBUG;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UniParser.OnParsingFinishListener, View.OnClickListener {

    private UniParser mUniParser; //Universal parser
    private ListView mLstChat; //Listview to show chat text
    private EditText mTxtInput;
    private Button mBtnSubmit;
    private Button mBtnSample;
    private List<ChatContent> mChatContentArray = new ArrayList<>(); //List contains all chat text
    private ChatContentAdapter mChatContentAdapter; //Listview adapter
    private final static String[] mSampleInputs = { //Sample chat texts
            "@chris you around?",
            "Good morning! (megusta) (coffee)",
            "Olympics are starting soon; http://www.nbcolympics.com",
            "@bob @john (success) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLstChat = (ListView) findViewById(R.id.lst_chat);
        mTxtInput = (EditText) findViewById(R.id.txt_input);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        mBtnSample = (Button) findViewById(R.id.btn_sample);

        //Set click listener for two button
        mBtnSubmit.setOnClickListener(this);
        mBtnSample.setOnClickListener(this);

        //Setup listview content
        mChatContentAdapter = new ChatContentAdapter(this, -1, -1, mChatContentArray);
        mLstChat.setAdapter(mChatContentAdapter);

        //Setup universal parser
        Context context = this;
        UniParser.OnParsingFinishListener parsingFinishListener = this;
        mUniParser = new UniParser(context, parsingFinishListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUniParser.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Check network status
        Network.showNotificationIfNoNetwork(this);
    }

    @Override
    public void OnParsingFinished(JSONObject outputJSON, int requestCode) {
        //A parsing task with request code has just done. Take the result now

        //Build json string
        String jsonString = "";
        try {
            jsonString = outputJSON.toString(4);//IndentSpace=4
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DEBUG.show("output[" + requestCode + "]:" + jsonString);

        //Skip if this activity is gone
        if (isFinishing() || isDestroyed()) return;

        //Updating listview content for this parsing task upon request code
        if (requestCode < mChatContentArray.size()) {

            //Update chat content list
            mChatContentArray.get(requestCode).Content = jsonString.replaceAll("\\\\", "");
            mChatContentArray.get(requestCode).IsParsingDone = true;
            mChatContentAdapter.notifyDataSetChanged();
            if (requestCode == mChatContentArray.size() - 1)//Scroll to last item
                mLstChat.smoothScrollToPosition(requestCode);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //Chat submit
            case R.id.btn_submit:
                String input = mTxtInput.getText().toString();
                mTxtInput.setText("");
                if (!input.isEmpty()) {
                    insertNewChatContent(input);
                }
                break;
            //Sample chat texts
            case R.id.btn_sample:
                for (String sample : mSampleInputs)
                    insertNewChatContent(sample);
                //Hide soft keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                break;
        }
    }

    /**
     * Bundle of Chat content to store its type (input by user or parsed result) and parsing status
     */
    static class ChatContent {
        String Content = "";
        boolean IsOriginal = false;
        boolean IsParsingDone = false;
    }

    /**
     * Insert a new chat text to the list and request uni parser to process it
     *
     * @param content Chat text content
     */
    private void insertNewChatContent(String content) {
        //Create chat content for user input
        ChatContent chatOriginalContent = new ChatContent();
        chatOriginalContent.Content = content;//
        chatOriginalContent.IsOriginal = true;
        mChatContentArray.add(chatOriginalContent);

        //Create a view holder for parsed result
        mChatContentArray.add(new ChatContent());
        int requestId = mChatContentArray.size() - 1;

        //Refresh listview
        mChatContentAdapter.notifyDataSetChanged();
        mLstChat.smoothScrollToPosition(requestId);

        //Request universal parser to process the task
        mUniParser.parser(chatOriginalContent.Content, requestId);
    }

    /**
     * Chat content adapter to build views for listview of chat text
     */
    static class ChatContentAdapter extends ArrayAdapter<ChatContent> {

        /**
         * Listview row view holder
         */
        static class ChatItemViewHolder {
            View layOrigin;
            View layResult;
            TextView txtOrigin;
            TextView txtResult;
        }

        //Referent of chat content list
        List<ChatContent> dataList;

        public ChatContentAdapter(Context context, int resource, int textViewResourceId, List<ChatContent> objects) {
            super(context, resource, textViewResourceId, objects);
            dataList = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChatContent chatContent = dataList.get(position);
            ChatItemViewHolder viewHolder = null;
            //Build a new view
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.list_item, null);
                viewHolder = new ChatItemViewHolder();
                viewHolder.txtOrigin = (TextView) view.findViewById(R.id.txt_original);
                viewHolder.txtResult = (TextView) view.findViewById(R.id.txt_result);
                viewHolder.layOrigin = view.findViewById(R.id.layout_original);
                viewHolder.layResult = view.findViewById(R.id.layout_result);
                convertView = view;
                convertView.setTag(viewHolder);
            }

            if (viewHolder == null) {
                viewHolder = (ChatItemViewHolder) convertView.getTag();
            }
            //If chat text is user input
            if (chatContent.IsOriginal) {
                viewHolder.layResult.setVisibility(View.GONE);
                viewHolder.layOrigin.setVisibility(View.VISIBLE);
                viewHolder.txtOrigin.setText(chatContent.Content);
            } else {
                //If chat text is parsed result
                viewHolder.layResult.setVisibility(View.VISIBLE);
                viewHolder.layOrigin.setVisibility(View.GONE);
                if (chatContent.IsParsingDone) //Parsing is done
                    viewHolder.txtResult.setText(chatContent.Content);
                else //Parsing...
                    viewHolder.txtResult.setText(R.string.chat_parsing);
            }

            return convertView;
        }
    }
}
