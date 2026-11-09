---
jahia-csrf-guard: patch
---

The CSRFGuard script is now added to a page for every spelling of a URL that a configured pattern covers. The filter matched a pattern against the raw request URL. A page reached through a spelling that the servlet mapper folds away, such as a repeated slash, was served without the script.
