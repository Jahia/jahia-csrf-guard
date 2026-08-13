---
jahia-csrf-guard: patch
---

Matched URL patterns and whitelists against the resolved path, so an entry covers every spelling of the URL it names.

Narrowed the exemption the module applies on its own to the SAML callback URL, `*callback.saml`, which is the SAML endpoint content changes arrive on. A site whose identity provider returns its response to another URL must list that URL in the `crossSiteWriteWhitelist` property of a module configuration — the setting to check is **Incoming Target Url**, whose default is `/home.callback.saml`.
