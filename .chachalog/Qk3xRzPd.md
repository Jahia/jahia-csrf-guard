---
# Allowed version bumps: patch, minor, major
jahia-csrf-guard: minor
---

Restricted content changes to requests that the browser reports as coming from the site itself.

Integrations that post to Jahia from another site, such as an identity provider or a notification service, must be listed in the `fetchMetadataWhitelist` property of a module configuration to keep working — the SAML callback already is. The check can be turned off by setting `fetchMetadata.enabled` to `false` in the module's global configuration.
