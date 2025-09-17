---
page:
  $path: /sites/academy/home/documentation/jahia/8_2/developer/cms-security/jahia-csrf-guard
  jcr:title: CSRF guard
  j:templateName: documentation
content:
  $subpath: document-area/content
---

Installed by default, Jahia CSRF Guard is a wrapper around the [OWASP CSRFGuard library](https://owasp.org/www-project-csrfguard/), providing CSRF token protection for all Jahia Actions with the usage of secure random tokens.

This mean of protection injects a unique token in HTML pages, when a user perform an action on that page, the token is submitted and validated by the backend receiving the request. This ensures that requests are actually submitted by the user who performed the operation.

### Overview

When enabled on a site, the jahia-csrf-guard module injects a javascript script inside pages for **authenticated users**, the role of this script is to add a CSRFTOKEN parameter in various HTML tags and opertaions.

The following elements are modified by the script:
 - an hidden `CSRFTOKEN` form input is added to HTML forms
 - a `CSRFTOKEN` parameter is added to `src` or `href` attributes
 - a `CSRFTOKEN` property is added to HTTP headers releated to XHR calls.

By default, all actions ending with a `.do` will attempt to validate tokens injected by the previously described mechanism. If the token is absent or does not match, the following message is displayed: `[CsrfGuard] - potential cross-site request forgery (CSRF) attack thwarted ....  ` in the browser console.

The version of OWASP CSRFGuard evolves alongside releases of the jahia-csrf-guard module, you can find the version embedded on the [module's pom.xml (link)](https://github.com/Jahia/jahia-csrf-guard/blob/master/pom.xml#L42).

:::warning
Note that the javascript code that will be injected into the pages is a slightly modified version of the original OWASP project, this was done to better integrate with Jahia.
:::

### Module Configuration

The module configuration is entirely based upon the OWASP's configuration [extensively detailed in their GitHub repository](https://github.com/aramrami/OWASP-CSRFGuard/blob/master/csrfguard/src/main/resources/csrfguard.properties). This page will provide concrete sample and use cases, but you are encouraged to review [the official csrfguart.properties](https://github.com/aramrami/OWASP-CSRFGuard/blob/master/csrfguard/src/main/resources/csrfguard.properties) file for more details about all of the available options.

All the configuration and registration of the OWASP library is done in the module using OSGI configuration.

Two level of configuration are possible:
- a global level applied across all sites on the platform. You can modify it editing the properties file located at: `digital-factory-data/karaf/etc/org.owasp.csrfguard.cfg`
- a per module configuration that manage fine grain configuration patterns. You can inject these by creating a file in your module's codebase at this location: `src/main/resources/META-INF/configurations/org.jahia.modules.jahiacsrfguard-test-module.cfg` (replacing `test-module` with your module's artifactId).

Configuration stored at a module level overwrite the global configuration.

### Use cases

#### Disabling CSRF-guard for an action

When developing your own module, there may be cases where a CSRF token is not needed for an action, for instance when the action does not trigger any state changes.

In such cases you would create a configuration in your module and whitelist the corresponding action (see details about creating a module configuration above).

```
whitelist = *.action1.do,*.action2.do
```

You can find an example of such a configuration in the [saml-authentication-valve codebase](https://github.com/Jahia/saml-authentication-valve/blob/dd3b68c1bc7fba48de8eca4444861ac516ec5bc2/src/main/resources/META-INF/configurations/org.jahia.modules.jahiacsrfguard-saml.cfg).

