package com.meetingplay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by KSB1 on 3/13/2016.
 */
public class Utility {

    private static Pattern pattern;
    private static Matcher matcher;
    //Email Pattern
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9\\-\\.]+\\.(com|org|net|mil|edu|COM|ORG|NET|MIL|EDU)$";

    public static boolean validate(String domain) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(domain);
        return matcher.matches();

    }

    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0 ? true: false;
    }
}
