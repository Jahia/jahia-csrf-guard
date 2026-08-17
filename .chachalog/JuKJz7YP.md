---
# Allowed version bumps: patch, minor, major
jahia-csrf-guard: patch
---

Fixed the module so it works on a Jahia server that runs Java 8.

On Java 8 the module started but protected no request, which left the site without CSRF protection. Jahia 8.1 supports Java 8, so a site that runs Jahia 8.1 on a Java 8 runtime was affected. Check the Java version of your Jahia server, and upgrade to this version of the module if that version is Java 8.
