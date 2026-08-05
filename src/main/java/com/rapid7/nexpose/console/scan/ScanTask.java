package com.rapid7.nexpose.console.scan;

import com.rapid7.nexpose.console.domain.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * A unit of scan work: resolve targets, then run them through the engine pool.
 * Used by the console's asynchronous scan scheduler.
 */
public class ScanTask implements Callable<List<Asset>> {

    private static final Logger log = LoggerFactory.getLogger(ScanTask.class);

    private final ScanEnginePool pool;
    private final ScanEngine engine;
    private final ScanTargetParser parser;
    private final List<String> targets;

    public ScanTask(ScanEnginePool pool, ScanEngine engine, ScanTargetParser parser, List<String> targets) {
        this.pool = pool;
        this.engine = engine;
        this.parser = parser;
        this.targets = targets;
    }

    @Override
    public List<Asset> call() {
        log.info("ScanTask starting for {} target expression(s)", targets.size());
        List<String> hosts = parser.resolve(targets);
        List<Asset> assets = pool.runScan(engine, hosts);
        log.info("ScanTask finished: {} asset(s) discovered", assets.size());
        return assets;
    }
}
