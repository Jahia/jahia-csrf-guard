---
# Allowed version bumps: patch, minor, major
jahia-csrf-guard: major
---

Raised the minimum supported Jahia version to 8.2.1.1, which also raises the minimum Java version to 11.

On an older Jahia, the module does not start. Jahia accepts the module and then leaves it stopped, and no CSRF protection is applied to the site.

If you run Jahia 8.1.x, 8.2.0.x or 8.2.1.0, upgrade Jahia to 8.2.1.1 or later first. Then install this version of the module, and check that it reports the started state. If you already run Jahia 8.2.1.1 or later, install it as usual.
