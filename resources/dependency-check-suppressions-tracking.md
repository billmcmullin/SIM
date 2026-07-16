# Dependency-Check Suppression Tracking

This file tracks temporary/targeted OWASP Dependency-Check suppressions and their removal criteria.

## Active Suppressions

- Scope: `pkg:maven/com.sim.chatserver/sim-core@*`
- CVEs: `CVE-2026-3431`, `CVE-2026-3432`
- Added: `2026-07-16`
- Why suppressed: False-positive mapping to SimStudio advisories; module is internal SIM artifact, not SimStudio.
- Remove when: Dependency-Check stops mapping these CVEs to sim-core, or SimStudio becomes a real dependency (then remediate by upgrade).
- Review cadence: Quarterly and after Dependency-Check engine upgrades.

## Verification Workflow

1. Run aggregate scan normally.
2. Re-run scan with suppression temporarily removed/disabled for this entry.
3. If finding no longer appears without suppression, delete suppression permanently.

Example command:

```bash
mvn org.owasp:dependency-check-maven:12.2.2:aggregate \
  -DnvdApiKey=<API KEY> \
  -DdataDirectory=<DC_DB_DIRECTORY>
```
