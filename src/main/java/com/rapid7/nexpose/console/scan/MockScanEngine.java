package com.rapid7.nexpose.console.scan;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.domain.Vulnerability;
import com.rapid7.nexpose.console.exception.ScanEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A deterministic mock scan engine. For each host address it fabricates an asset
 * with a plausible OS fingerprint and a seeded set of vulnerabilities, so the
 * rest of the pipeline (risk scoring, correlation, reporting) has realistic data
 * to operate on without touching a network.
 *
 * <p>Well-known "sentinel" targets are used to make specific defects reachable on
 * demand without breaking ordinary scans:</p>
 * <ul>
 *   <li>{@code 0.0.0.0} &rarr; the engine throws {@link ScanEngineException}. Because
 *       {@link ScanEnginePool#runScan} leaks its permit on failure (NEX-3107),
 *       repeating this scan exhausts the pool.</li>
 *   <li>{@code 198.51.100.7} (TEST-NET-2) &rarr; returns a live asset with an
 *       <em>empty</em> (non-null) vulnerability list, which makes the risk
 *       calculator divide by zero (NEX-3102).</li>
 * </ul>
 * <p>Independently, roughly one host in six is returned live-but-unfingerprinted
 * with a {@code null} vulnerability list — the data shape that makes the XML
 * report generator throw a NullPointerException (NEX-3101).</p>
 */
public class MockScanEngine implements ScanEngine {

    private static final Logger log = LoggerFactory.getLogger(MockScanEngine.class);

    public static final String SENTINEL_ENGINE_ERROR = "0.0.0.0";
    public static final String SENTINEL_EMPTY_VULNS = "198.51.100.7";

    private static final String[] OS_FINGERPRINTS = {
            "Ubuntu Linux 22.04", "Windows Server 2019", "Red Hat Enterprise Linux 8",
            "CentOS 7", "Debian 12", "Windows 10 Pro", "VMware ESXi 7.0"
    };

    private static final String[][] VULN_CATALOG = {
            {"ssl-cve-2016-2183", "SSL/TLS Birthday attack (SWEET32)", "7.5", "443", "tcp"},
            {"tls-dh-prime-under-2048", "Diffie-Hellman prime under 2048 bits", "5.3", "443", "tcp"},
            {"ssh-weak-kex", "SSH weak key exchange algorithms", "4.3", "22", "tcp"},
            {"smb-signing-not-required", "SMB signing not required", "5.0", "445", "tcp"},
            {"http-trace-method-enabled", "HTTP TRACE method enabled", "3.5", "80", "tcp"},
            {"cve-2021-44228", "Apache Log4j2 RCE (Log4Shell)", "10.0", "8080", "tcp"},
            {"cve-2017-0144", "EternalBlue SMB RCE", "9.3", "445", "tcp"},
            {"rdp-nla-not-enabled", "RDP Network Level Authentication not enabled", "6.5", "3389", "tcp"}
    };

    private final String engineName;

    public MockScanEngine(String engineName) {
        this.engineName = engineName;
    }

    @Override
    public String name() {
        return engineName;
    }

    @Override
    public List<Asset> scan(List<String> hostAddresses) {
        log.info("[{}] starting scan of {} host(s)", engineName, hostAddresses.size());
        List<Asset> assets = new ArrayList<>();
        for (String host : hostAddresses) {
            if (SENTINEL_ENGINE_ERROR.equals(host)) {
                log.error("[{}] engine fault scanning reserved address {}", engineName, host);
                throw new ScanEngineException(
                        "Scan engine failed to reach target " + host + " (engine channel reset)");
            }
            Random rnd = new Random(host.hashCode());   // deterministic per host
            Asset asset = new Asset();
            asset.setIpAddress(host);
            asset.setHostName(host.replace('.', '-') + ".lab.rapid7.com");
            asset.setOperatingSystem(OS_FINGERPRINTS[Math.abs(rnd.nextInt()) % OS_FINGERPRINTS.length]);
            asset.setLive(true);

            if (SENTINEL_EMPTY_VULNS.equals(host)) {
                // Live, fingerprinted, but zero vulns -> empty (non-null) list.
                asset.setVulnerabilities(new ArrayList<>());
                log.debug("[{}] host {} fingerprinted with 0 vulnerabilities (empty list)", engineName, host);
            } else if (Math.abs(rnd.nextInt()) % 6 == 0) {
                // ~1 in 6: discovered but not fingerprinted -> null vuln list.
                asset.setVulnerabilities(null);
                log.debug("[{}] host {} live but not fingerprinted (no vuln list)", engineName, host);
            } else {
                int vulnCount = 1 + (Math.abs(rnd.nextInt()) % (VULN_CATALOG.length - 1));
                List<Vulnerability> vulns = new ArrayList<>();
                for (int i = 0; i < vulnCount; i++) {
                    String[] spec = VULN_CATALOG[(i + Math.abs(rnd.nextInt())) % VULN_CATALOG.length];
                    Vulnerability v = new Vulnerability(spec[0], spec[1],
                            Double.parseDouble(spec[2]), Integer.parseInt(spec[3]), spec[4]);
                    v.setCve(spec[0].toUpperCase());
                    vulns.add(v);
                }
                asset.setVulnerabilities(vulns);
                log.debug("[{}] host {} fingerprinted as '{}' with {} vuln(s)",
                        engineName, host, asset.getOperatingSystem(), vulns.size());
            }
            assets.add(asset);
        }
        log.info("[{}] scan produced {} asset(s)", engineName, assets.size());
        return assets;
    }
}
