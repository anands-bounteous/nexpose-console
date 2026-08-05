package com.rapid7.nexpose.console.domain;

/** Base type for a section of a generated report. */
public abstract class ReportSection {

    private final String title;

    protected ReportSection(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /** The XML element name this section renders as. */
    public abstract String elementName();
}
