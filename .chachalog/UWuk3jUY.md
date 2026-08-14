---
# Allowed version bumps: patch, minor, major
jahia-csrf-guard: major
---

Raised the minimum supported Jahia version to 8.2.1.1, which also raises the minimum Java version to 11.

Earlier releases declared support for Jahia 8.1.7.0 and later. That was incorrect. The module cannot start on any Jahia older than 8.2.1.1: Jahia accepts the module, then leaves it stopped instead of started, and no CSRF protection is applied to the site.

You are affected if you run Jahia 8.1.x, 8.2.0.x or 8.2.1.0. To move to this version, upgrade Jahia to 8.2.1.1 or later first, then install the module and check that it reports the started state. If you already run Jahia 8.2.1.1 or later, install it as usual.
