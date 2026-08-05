---
# Allowed version bumps: patch, minor, major
jahia-csrf-guard: minor
---

Restricted content changes to requests that the browser reports as coming from the site itself.

Integrations that post to Jahia from another site, such as an identity provider or a notification service, must be listed in the `fetchMetadataWhitelist` property of a module configuration to keep working — the SAML callback already is. The check can be turned off by setting `fetchMetadata.enabled` to `false` in the module's global configuration.

URL pattern and whitelist matching now strips matrix parameters (`;name=value`) from the request path before matching, so a pattern or whitelist entry is compared against the path Jahia resolves. This also applies to the existing `urlPatterns` and `whitelist` used by token validation.
