package com.web2mine.chatpaser.parser;

import android.content.Context;

import com.web2mine.chatpaser.common.CONSTANT;
import com.web2mine.chatpaser.common.DEBUG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing any link from chat text. This class inherits ItemParse to customize parsing pattern and type
 */
public class LinkParser extends ItemParser {

    //The pattern returning all website links from chat text.
    private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)", Pattern.CASE_INSENSITIVE);
    //The pattern to parse page title from web html source
    private static final Pattern PAGE_TITLE_PATTERN = Pattern.compile("(?<=<title>).*(?=<\\/title>)", Pattern.CASE_INSENSITIVE);

    @Override
    protected Pattern getPattern() {
        return URL_PATTERN;
    }

    @Override
    public String getType() {
        return ItemType.LINK;
    }

    public LinkParser(Context context) {
        super(context);
    }

    /**
     * For link item, we need to load the link and parse the page title before building JSON result
     * @param list Parsed result list
     * @return JSON Array
     */
    @Override
    protected JSONArray buildJson(ArrayList<String> list) {

        //If list is empty, nothing to do
        if (list == null || list.size() == 0) {
            DEBUG.error("No input!");
            return null;
        }

        JSONArray jsonArray = new JSONArray(); //Result JSON
        //Load all link for page title
        for (String link : list) {
            String pageTitle = "";
            URL url = null;
            //Begin to load the link
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(link);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(15*1000); //Wait maximum 15s for connection
                urlConnection.setReadTimeout(60*1000); //Source reading timeout is 60s
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) {
                    //Parsing each line after reading
                    Matcher matcher = PAGE_TITLE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        pageTitle = matcher.group();
                        break; //Found it. Done
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection!=null)
                    urlConnection.disconnect();
            }

            //Build json for link this item
            try {
                JSONObject jsonLink = new JSONObject();
                jsonLink.put(CONSTANT.JSON_LINK_URL, link);
                jsonLink.put(CONSTANT.JSON_LINK_TITLE, pageTitle);
                jsonArray.put(jsonLink);

            } catch (JSONException e) {
                e.printStackTrace();
                DEBUG.error(getType() + ":" + e.getMessage());
                return null;
            }
        }

        //Return the result
        return jsonArray;
    }
}
