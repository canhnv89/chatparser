package com.web2mine.chatpaser.parser;

import android.content.Context;

import java.util.regex.Pattern;

/**
 * Parsing any emoticon from chat text. This class inherits ItemParse to customize parsing pattern and type
 */
public class EmoticonParser extends ItemParser {
    //This pattern will return all emoticon from (emoticon) but exclude the parenthesis.
    // Emoticons which are alphanumeric strings, no longer than 15 characters
    private static final Pattern EMOTICON_PATTERN = Pattern.compile("(?<=\\()[a-zA-Z0-9]{1,15}(?=\\))");

    @Override
    protected Pattern getPattern() {
        return EMOTICON_PATTERN;
    }

    @Override
    public String getType() {
        return ItemType.EMOTICON;
    }

    public EmoticonParser(Context context) {
        super(context);
    }
}
