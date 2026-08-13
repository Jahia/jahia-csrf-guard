---
jahia-csrf-guard: patch
---

Scoped the exemption the module applies on its own to the SAML callback URL, `*callback.saml`.

A site whose identity provider returns its response to another URL must list that URL in the `crossSiteWriteWhitelist` property of a module configuration. The setting to check is **Incoming Target Url**, whose default is `/home.callback.saml`.
