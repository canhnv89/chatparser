package com.web2mine.chatpaser.parser;

import android.content.Context;

import java.util.regex.Pattern;

/**
 * Parsing any mention from chat text. This class inherits ItemParse to customize parsing pattern and type
 */
public class MentionParser extends ItemParser {
    //This pattern will return all mentioned name from @name
    private static final Pattern MENTION_PATTERN = Pattern.compile("(?<=@)[a-zA-Z0-9]+");

    public MentionParser(Context context) {
        super(context);
    }

    @Override
    protected Pattern getPattern() {
        return MENTION_PATTERN;
    }

    @Override
    public String getType() {
        return ItemType.MENTION;
    }
}
