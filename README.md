# nexpose-console

Shared library module for the **Nexpose Security Console** proof-of-concept. It
holds the code that the web application (`nexpose-root`) depends on:

- **domain/** — `Asset`, `Vulnerability`, `Scan`, `User`, report sections, enums.
- **exception/** — `NexposeException` base type + concrete custom exceptions
  (scan, report, auth, LDAP, asset, risk, correlation), each with a stable error code.
- **report/** — `XmlReportGenerator`, `ReportEngine`.
- **scan/** — `ScanEngine` interface, `MockScanEngine`, `ScanEnginePool`,
  `ScanTargetParser`, `ScanTask`.
- **correlation/** — `AssetCorrelator`, `DomainAggregator`.
- **risk/** — `RiskCalculator`.
- **auth/** — `LocalAuthenticator` (built-in `nxadmin`), `LdapAuthenticator` (JNDI),
  `LdapSettings`.
- **util/** — `IpAddressUtils`, `DateUtils`, `XmlUtils`.

## Build

This is a plain library JAR (Spring Boot dependency management, **no** executable
repackaging). It must be installed to the local Maven repository **before**
building `nexpose-root`:

```bash
mvn clean install
```

Requires **JDK 17** and Maven 3.9+.

## Planted defects that live in this module

| ID       | Type | Class / method |
|----------|------|----------------|
| NEX-3101 | code | `report/XmlReportGenerator.appendAsset` — NPE on null vulnerability list |
| NEX-3102 | code | `risk/RiskCalculator.averageCvss` — divide by zero on empty vuln list |
| NEX-3103 | code | `util/IpAddressUtils.expandCidr` — `NumberFormatException` on bad CIDR |
| NEX-3104 | code | `correlation/AssetCorrelator.mergeDuplicates` — `ConcurrentModificationException` |
| NEX-3105 | code | `report/ReportEngine.assetSectionOf` — `ClassCastException` |
| NEX-3106 | code | `util/DateUtils.parseScanTimestamp` — timezone/offset parse failure |
| NEX-3107 | code | `scan/ScanEnginePool.runScan` — permit leak on scan failure |
| NEX-3108 | config (surfaced here) | `auth/LdapAuthenticator` — misconfigured LDAP URL/base DN |

See each class's Javadoc for the exact line and the fix.
