package com.web2mine.chatpaser;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.web2mine.chatpaser.parser.ItemParser;
import com.web2mine.chatpaser.parser.MentionParser;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class MentionAndroidUnitTest  implements ItemParser.OnResultListener {
    final CountDownLatch signal = new CountDownLatch(1);
    JSONArray mJsonObject;

    private Context context;
    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }
    @Test
    public void parseInput_OK() throws Exception {
        ItemParser mentionParser = new MentionParser(context);
        mentionParser.setOnResultListener(this);
        mentionParser.parse(0, "@bob @john (success) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016");
        signal.await();
        assertEquals(mJsonObject.toString(),"[\"bob\",\"john\"]");
    }

    @Test
    public void parseInput_None() throws Exception {
        ItemParser mentionParser = new MentionParser(context);
        mentionParser.setOnResultListener(this);
        mentionParser.parse(0, "@~!akdfa @ @@ @$%#john (success) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016");
        signal.await();
        assertEquals(mJsonObject, null);
    }

    @Override
    public void onDone(int taskId, JSONArray jsonObject, ItemParser itemParser) {
        mJsonObject = jsonObject;
        signal.countDown();
    }
}