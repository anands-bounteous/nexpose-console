package com.rapid7.nexpose.console.correlation;

import com.rapid7.nexpose.console.domain.Asset;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Regression test for NEX-3104 / SI-5: {@code mergeDuplicates} must de-duplicate
 * a scan containing the same asset twice without raising a server error.
 *
 * <p>Before the fix, {@code mergeDuplicates} removed the duplicate from the very
 * list it was iterating with an enhanced for-loop, so the next
 * {@code Iterator.next()} threw {@link java.util.ConcurrentModificationException}
 * (wrapped as {@code CorrelationException}). The duplicate is placed at index 1
 * with two further distinct assets after it: this ordering guarantees the
 * iterator advances past the removal and the CME fires (an ordering where the
 * duplicate is the second-to-last element would end the loop early and mask the
 * bug).</p>
 */
class AssetCorrelatorTest {

    @Test
    void mergeDuplicates_withRepeatedAsset_dedupesWithoutServerError() {
        AssetCorrelator correlator = new AssetCorrelator();

        Asset a1 = new Asset("10.0.0.1", "host-a", "linux");
        Asset a1dup = new Asset("10.0.0.1", "host-a", "linux"); // same IP -> duplicate
        Asset b = new Asset("10.0.0.2", "host-b", "linux");
        Asset c = new Asset("10.0.0.3", "host-c", "linux");

        List<Asset> assets = new ArrayList<>(List.of(a1, a1dup, b, c));

        List<Asset> result =
                assertDoesNotThrow(() -> correlator.mergeDuplicates(assets));

        assertEquals(3, result.size(), "duplicate IP should be removed");
        assertEquals(List.of("10.0.0.1", "10.0.0.2", "10.0.0.3"),
                result.stream().map(Asset::getIpAddress).toList(),
                "first occurrence of each IP should survive, in order");
    }
}
