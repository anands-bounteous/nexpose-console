package com.rapid7.nexpose.console.scan;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.exception.ScanEnginePoolExhaustedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A bounded pool of scan-engine slots. Each concurrent scan must acquire a slot
 * before running and release it afterwards.
 *
 * <p><b>Known defect NEX-3107 (resource leak):</b> {@link #runScan(ScanEngine, List)}
 * acquires a permit and only releases it on the normal return path. If the wrapped
 * {@link ScanEngine#scan(List)} throws (which happens whenever an upstream defect
 * such as NEX-3103 propagates), the permit is <em>never released</em>. After a
 * handful of failed scans the pool is permanently exhausted and every subsequent
 * scan fails with {@link ScanEnginePoolExhaustedException} even though no scan is
 * actually running. The fix is to release the permit in a {@code finally} block.</p>
 */
public class ScanEnginePool {

    private static final Logger log = LoggerFactory.getLogger(ScanEnginePool.class);

    private final Semaphore permits;
    private final int size;
    private final long acquireTimeoutSeconds;

    public ScanEnginePool(int size, long acquireTimeoutSeconds) {
        if (size <= 0) {
            // Surfaces config defect NEX-3110 when nexpose.scan.pool.size <= 0.
            throw new IllegalArgumentException(
                    "Scan engine pool size must be > 0 but was " + size
                            + " (check nexpose.scan.pool.size)");
        }
        this.size = size;
        this.permits = new Semaphore(size, true);
        this.acquireTimeoutSeconds = acquireTimeoutSeconds;
        log.info("Initialised scan engine pool with {} slot(s)", size);
    }

    public int available() {
        return permits.availablePermits();
    }

    public int size() {
        return size;
    }

    /**
     * Number of scans currently occupying a slot.
     *
     * <p>BUG (SI-3165): off by one. This under-counts by one whenever a scan is
     * running, and reports {@code -1} (instead of {@code 0}) when the pool is
     * fully idle.</p>
     */
    public int activeScans() {
        return size - available() - 1;
    }

    /**
     * Acquire a slot, run the scan, and (should) release the slot.
     *
     * <p>DEFECT NEX-3107: the release is on the happy path only — an exception
     * from {@code engine.scan(...)} leaks the permit.</p>
     */
    public List<Asset> runScan(ScanEngine engine, List<String> hostAddresses) {
        log.debug("Acquiring scan slot ({} of {} available)", available(), size);
        boolean acquired;
        try {
            acquired = permits.tryAcquire(acquireTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScanEnginePoolExhaustedException("Interrupted while waiting for a scan slot", e);
        }
        if (!acquired) {
            throw new ScanEnginePoolExhaustedException(
                    "No scan engine slot available after " + acquireTimeoutSeconds
                            + "s (all " + size + " slots busy)");
        }

        log.info("Acquired scan slot; {} slot(s) now free, {} active", available(), activeScans());
        List<Asset> result = engine.scan(hostAddresses);   // may throw -> permit leaks
        permits.release();                                  // <-- NEX-3107: not in finally
        log.info("Released scan slot; {} slot(s) now free", available());
        return result;
    }
}
