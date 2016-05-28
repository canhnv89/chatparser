package com.web2mine.chatpaser;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.web2mine.chatpaser.parser.ItemParser;
import com.web2mine.chatpaser.parser.LinkParser;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LinkAndroidUnitTest implements ItemParser.OnResultListener {
    final CountDownLatch signal = new CountDownLatch(1);
    JSONArray mJsonObject;


    private Context context;
    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }

    @Test
    public void parseInput_OK() throws Exception {
        ItemParser linkParser = new LinkParser(context);
        linkParser.setOnResultListener(this);
        linkParser.parse(0, "@bob @john (success) such a cool feature; https://twitter.com/jdorfman/status/430511497475670016");
        signal.await();
        assertEquals(mJsonObject.toString(),"[{\"url\":\"https:\\/\\/twitter.com\\/jdorfman\\/status\\/430511497475670016\",\"title\":\"Twitter\"}]");
    }

    @Test
    public void parseInput_None() throws Exception {
        ItemParser linkParser = new LinkParser(context);
        linkParser.setOnResultListener(this);
        linkParser.parse(0, "such a cool feature; httxps://twitter.com/jdorfman/status/430511497475670016 abc://twitter.com/jdorfman/status/430511497475670016 www.twitter.com/jdorfman/status/430511497475670016");
        signal.await();
        assertEquals(mJsonObject,null);
    }

    @Test
    public void parseInput_multi_OK() throws Exception {
        ItemParser linkParser = new LinkParser(context);
        linkParser.setOnResultListener(this);
        linkParser.parse(0, "httxps://twitter.com/ https://twitter.com/jdorfman/status/430511497475670016 http://www.nbcolympics.com http://www.nolympics.comsafasfddagagasdg");
        signal.await();
        assertEquals(mJsonObject.toString(),"[{\"url\":\"https:\\/\\/twitter.com\\/jdorfman\\/status\\/430511497475670016\",\"title\":\"Twitter\"},{\"url\":\"http:\\/\\/www.nbcolympics.com\",\"title\":\"2016 Rio Olympic Games | NBC Olympics\"}]");
    }

    @Override
    public void onDone(int taskId, JSONArray jsonObject, ItemParser itemParser) {
        mJsonObject = jsonObject;
        signal.countDown();
    }
}