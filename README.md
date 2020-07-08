# Jahia CSRF Guard

This module will add CSRF token protection on all call to a Jahia Action.
It's based on [OWASP CSRFGuard library](https://owasp.org/www-project-csrfguard/). 

### How to upgrade OWASP CSRFGuard

#### Minor version
Upgrade to a minor version should be seamless but you will have to compare the csrfguard.template.js with its original version to merge the changes.

#### Major version
It will depend of the changelog and breaking changes of the version, but again here do not forget to compare the csrfguard.template.js with its original version to merge the changes.
