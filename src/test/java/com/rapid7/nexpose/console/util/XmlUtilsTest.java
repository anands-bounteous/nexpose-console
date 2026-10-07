package com.rapid7.nexpose.console.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class XmlUtilsTest {
    @Test
    void escapesApostrophe() {
        assertEquals("O&apos;Brien-WKS-04", XmlUtils.escape("O'Brien-WKS-04"));
    }

    @Test
    void escapesOtherSpecialCharacters() {
        assertEquals("&amp;&lt;&gt;&quot;", XmlUtils.escape("&<>\""));
    }
}
