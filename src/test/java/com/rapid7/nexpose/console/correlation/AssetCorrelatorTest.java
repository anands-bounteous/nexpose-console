package com.rapid7.nexpose.console.correlation;

import com.rapid7.nexpose.console.domain.Asset;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AssetCorrelatorTest {

    /**
     * Reproduces NEX-3104/SI-18: with a duplicate IP encountered at index
     * (size - 2), removing during the enhanced for-loop does not throw CME
     * (cursor catches up to the shrunk size exactly), but Asset.equals()
     * compares by IP only, so assets.remove(duplicate) removes the *first*
     * matching element - the correct first-seen asset - not the duplicate.
     */
    @Test
    void mergeDuplicatesKeepsFirstSeenAssetInsteadOfDroppingIt() {
        Asset firstSeen = new Asset("10.0.0.1", "first-host", "Linux");
        Asset duplicate = new Asset("10.0.0.1", "dup-host", "Linux");
        Asset distinct = new Asset("10.0.0.2", "third-host", "Windows");
        List<Asset> assets = new ArrayList<>(List.of(firstSeen, duplicate, distinct));

        List<Asset> result = new AssetCorrelator().mergeDuplicates(assets);

        assertEquals(2, result.size());
        assertSame(firstSeen, result.get(0));
        assertSame(distinct, result.get(1));
    }
}
