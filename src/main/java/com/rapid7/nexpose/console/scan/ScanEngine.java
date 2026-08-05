package com.rapid7.nexpose.console.scan;

import com.rapid7.nexpose.console.domain.Asset;

import java.util.List;

/** A scan engine capable of scanning a set of targets and returning assets. */
public interface ScanEngine {

    String name();

    /** Scan the given resolved host addresses and return discovered assets. */
    List<Asset> scan(List<String> hostAddresses);
}
