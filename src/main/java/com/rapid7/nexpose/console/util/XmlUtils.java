package com.rapid7.nexpose.console.util;

/** Small helpers for safe XML text/attribute escaping. */
public final class XmlUtils {

    private XmlUtils() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                // NOTE (SI-3151): apostrophes are intentionally left unescaped here
                // pending a decision on quote style; see ticket for the malformed-XML impact.
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
