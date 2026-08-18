# jahia-csrf-guard Changelog

## 1.0.0

### Breaking Changes

* Raised the minimum supported Jahia version to 8.2.1.1, which also raises the minimum Java version to 11.

  If you run Jahia 8.1.x, 8.2.0.x or 8.2.1.0, upgrade Jahia to 8.2.1.1 or later first. Then install this version of the module, and check that it reports the started state. If you already run Jahia 8.2.1.1 or later, install it as usual.

### New Features

* Restricted content changes to requests that the browser reports as coming from the site itself.

  Integrations that post to Jahia from another site, such as an identity provider or a notification service, must be listed in the `crossSiteWriteWhitelist` property of a module configuration to keep working — the SAML callback already is. The check can be turned off by setting `crossSiteWriteProtection.enabled` to `false` in the module's global configuration.

  URL pattern and whitelist matching now strips matrix parameters (`;name=value`) from the request path before matching, so a pattern or whitelist entry is compared against the path Jahia resolves. This also applies to the existing `urlPatterns` and `whitelist` used by token validation.

### Bug Fixes

* Fix race condition in the config that lead to a NPE. (#167)

* Matched URL patterns and whitelists against the resolved path, so an entry covers every spelling of the URL it names.

* Scoped the exemption the module applies on its own to the SAML callback URL, `*callback.saml`.

  A site whose identity provider returns its response to another URL must list that URL in the `crossSiteWriteWhitelist` property of a module configuration. The setting to check is **Incoming Target Url**, whose default is `/home.callback.saml`.
