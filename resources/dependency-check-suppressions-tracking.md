# Dependency-Check Suppression Tracking

This file tracks temporary/targeted OWASP Dependency-Check suppressions and their removal criteria.

## Active Suppressions

### 1) Internal SIM modules misidentified as SimStudioAI

- Scope: `^pkg:maven/com.sim.chatserver/sim-(core|email|web)@.*$` (regex)
- CVEs: `CVE-2025-10096`, `CVE-2025-15099`, `CVE-2025-7107`, `CVE-2025-7114`, `CVE-2025-9800`, `CVE-2025-9801`, `CVE-2025-9805`, `CVE-2026-3431`, `CVE-2026-3432`
- Added/Updated: `2026-07-17`
- Why suppressed:
  - Dependency-Check matched internal Maven coordinates (`com.sim.chatserver:*`) to unrelated SimStudioAI "sim" advisories due naming similarity.
  - Report descriptions reference SimStudioAI web app paths and components (for example `apps/sim/...` TypeScript/Next.js routes) that do not exist in this Java codebase.
  - These are false-positive product matches, not vulnerable dependencies present in SIM.
- Remove when:
  - Dependency-Check no longer maps these CVEs to `com.sim.chatserver` artifacts.
  - If SimStudioAI is introduced as a real dependency, remove suppression and remediate via upgrade/patch.
- Review cadence: Quarterly and after Dependency-Check engine/version upgrades.

### 2) PDFBox example-code CVEs not applicable to runtime/library usage

- Scope: `pkg:maven/org.apache.pdfbox/pdfbox@2.0.24`, `pkg:maven/org.apache.pdfbox/xmpbox@2.0.24`
- CVEs: `CVE-2026-23907`, `CVE-2026-33929`
- Added: `2026-07-17`
- Why suppressed:
  - Both CVEs describe a vulnerability in PDFBox **example application code** (`ExtractEmbeddedFiles`), not the core library APIs.
  - Verification evidence from this triage:
    - `pdfbox-2.0.24.jar` and `xmpbox-2.0.24.jar` contain no `org/apache/pdfbox/examples/*` entries.
    - Source search in `sim-core`, `sim-web`, and `sim-email` found no usage of `ExtractEmbeddedFiles` or `PDComplexFileSpecification` patterns associated with the advisory.
  - For this project context, this is a non-applicable finding.
- Remove when:
  - Dependency-Check refines these advisories so they no longer flag library-only usage.
  - The project imports/copies vulnerable example extraction logic.
  - Upgrading PDFBox/xmpbox beyond the affected range is completed and verified.
- Review cadence: Quarterly and after Dependency-Check engine/version upgrades.

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
