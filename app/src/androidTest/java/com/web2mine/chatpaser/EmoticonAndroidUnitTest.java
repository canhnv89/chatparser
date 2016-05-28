package com.web2mine.chatpaser;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.web2mine.chatpaser.parser.EmoticonParser;
import com.web2mine.chatpaser.parser.ItemParser;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class EmoticonAndroidUnitTest implements ItemParser.OnResultListener {
    final CountDownLatch signal = new CountDownLatch(1);
    JSONArray mJsonObject;

    private Context context;
    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }

    @Test
    public void parseInput_OK() throws Exception {
        ItemParser emoticonParser = new EmoticonParser(context);
        emoticonParser.setOnResultListener(this);
        emoticonParser.parse(0, "@bob @john (success) (1success) (success1) (13smile142) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016");
        signal.await();
        assertEquals(mJsonObject.toString(),"[\"success\",\"1success\",\"success1\",\"13smile142\"]");
    }

    @Test
    public void parseInput_None() throws Exception {
        ItemParser emoticonParser = new EmoticonParser(context);
        emoticonParser.setOnResultListener(this);
        emoticonParser.parse(0, "() (test_test) ($success) (#success) (successMoreThan15Words) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016");
        signal.await();
        assertEquals(mJsonObject, null);
    }

    @Override
    public void onDone(int taskId, JSONArray jsonObject, ItemParser itemParser) {
        mJsonObject = jsonObject;
        signal.countDown();
    }
}