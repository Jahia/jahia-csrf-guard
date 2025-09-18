---
page:
  $path: /sites/academy/home/documentation/jahia/8_2/developer/cms-security/jahia-csrf-guard
  jcr:title: CSRF guard
  j:templateName: documentation
content:
  $subpath: document-area/content
---

Installed by default, Jahia CSRF Guard is a wrapper around the [OWASP CSRFGuard library](https://owasp.org/www-project-csrfguard/), providing CSRF token protection for all Jahia Actions with the usage of secure random tokens.

This means of protection injects a unique token in HTML pages for **authenticated users**. When a user perform an action on that page, the token is submitted and validated by the backend receiving the request. This ensures that requests are actually submitted by the user who performed the operation, protecting against man-in-the-middle attacks.

The CSRF Guard module is only relevant in the context of authenticated user since its role is to protect against impersonation. Such mechanisms are not relevant for guest users, not being authenticated, their level of authorization is the same for all visitors (including potential attackers).

### Overview

When enabled on a site, the jahia-csrf-guard module injects a javascript script inside pages for **authenticated users**, the role of this script is to add a CSRFTOKEN parameter in various HTML tags and operations.

The following elements are modified by the script:
 - a hidden `CSRFTOKEN` form input is added to HTML forms
 - a `CSRFTOKEN` parameter is added to `src` or `href` attributes
 - a `CSRFTOKEN` property is added to HTTP headers related to XHR calls.

By default, all actions ending with a `.do` will attempt to validate tokens injected by the previously described mechanism. If the token is absent or does not match, the following message is displayed in the browser console: 

```
[CsrfGuard] - potential cross-site request forgery (CSRF) attack thwarted ....  
```

The version of the [OWASP CSRFGuard library](https://owasp.org/www-project-csrfguard/) evolves alongside releases of the jahia-csrf-guard module, you can find the version embedded on the [module's pom.xml (link)](https://github.com/Jahia/jahia-csrf-guard/blob/master/pom.xml#L42).

:::info
Note that the javascript code that will be injected into the pages is a slightly modified version of the original OWASP project, this was done to better integrate with Jahia.
:::

### Module Configuration

The module configuration is entirely based upon the OWASP's configuration [extensively detailed in their GitHub repository](https://github.com/aramrami/OWASP-CSRFGuard/blob/master/csrfguard/src/main/resources/csrfguard.properties). This page will provide concrete sample and use cases, but you are encouraged to review [the official csrfguard.properties](https://github.com/aramrami/OWASP-CSRFGuard/blob/master/csrfguard/src/main/resources/csrfguard.properties) file for more details about all of the available options.

All the configuration and registration of the OWASP library is done in the module using OSGI configuration.

Two level of configuration are possible:
- a global level applied across all sites on the platform. You can modify it editing the properties file located at: `digital-factory-data/karaf/etc/org.owasp.csrfguard.cfg`. Changes to the global configuration are loaded when a modification is done, without needing to stop the jahia-csrf-guard module, this can take up to 60s for changes to be picked up. These changes are automatically propagated on the cluster.
- a per module configuration that manage fine grain configuration patterns. You can inject these by creating a file in your module's codebase at this location: `src/main/resources/META-INF/configurations/org.jahia.modules.jahiacsrfguard-test-module.cfg` (replacing `test-module` with your module's artifactId).

Parameters present in configuration stored at a module level overwrite the global configuration.

:::info
As with any OSGI configuration, they can be created/edited either on the filesystem, via the OSGI console in Jahia Tools, or via the APIs (GraphQL, provisioning).
:::

### Use cases

#### Disabling CSRF guard entirely

If needed, you can disable CSRF guard module entirely with this parameter:

```
org.owasp.csrfguard.Enabled = false
```

This should be a temporary solution, it is not recommended to keep CSRF guard disabled.


#### Disabling CSRF-guard for an action

When developing your own module, it might be necessary to disable the CSRF token mechanism for a specific action, for instance when the action does not trigger any state changes, or when this action is aimed at being called by an external tool (such as curl).

In such cases you would create a configuration in your module and whitelist the corresponding action using this parameter:

```
whitelist = *.action1.do,*.action2.do
```

You can find an example of such a configuration in the [saml-authentication-valve codebase](https://github.com/Jahia/saml-authentication-valve/blob/dd3b68c1bc7fba48de8eca4444861ac516ec5bc2/src/main/resources/META-INF/configurations/org.jahia.modules.jahiacsrfguard-saml.cfg).

#### Referer check does not match the protocol

When the SSL termination is established on a reverse proxy but that proxy communicates with Jahia via HTTP, you may see the following error in logs:

```
ERROR [CsrfGuard] - Referer domain https://<your-page> does not match request domain: http://<your-page>
```

This means your environment is not configured properly, you can find more details about how to address this issue [on this page](/cms/{mode}/{lang}/sites/academy/contents/knowledge-base/2018/dx-links-are-not-in-https.html).

Alternatively, you could also set the following property:

```
org.owasp.csrfguard.JavascriptServlet.refererMatchProtocol = false
```

#### Using tokens per page

It is possible to enable the creation of random unique tokens per-page (and session) as opposed to just a unique per-session token, which then is the same on all pages. This in-depth defense strategy limit the impact of leaked CSRF tokens. 

With a leaked token per-session a CSRF attack could be carried out against any form or action in the entire webapp, as long as the victim's session is active. With a token per-page the CSRF attack could only be carried out against a small subset of resources.

Starting with Jahia 8.2.0, the token per-page implementation is activated by default. 

Note that it requires testing and possible code changes within custom templates or modules. You should especially check that the back button of the browser or a form re-submit in the same session still works as expected. Issues might also arise AJAX requests to actions are performed during the page initialization phase (see [this discussion](https://github.com/OWASP/www-project-csrfguard/issues/49#issuecomment-1006451596) for hints about possible solutions). 

The usage of tokens per-page can be deactivated in Jahia 8.1+ by setting the following property in /karaf/etc/org.owasp.csrfguard.cfg:

```
org.owasp.csrfguard.TokenPerPage = false
```

### Caching considerations

Starting from jahia-csrf-guard 4.2.0, CSRF guard javascript injection is disabled for guest users (unauthenticated users) by default.

This allows for better client-side caching and CDN optimization of pages, this behavior can be modified with the following property:

```
jahia.csrf-guard.bypassForGuest = false
```

For authenticated users (or when bypass is disabled), a `tag` parameter is added in the javascript URL to tweak client side caching performances. When javascript link is injected in pages, a hash (md5) of the targeted dynamic javascript is included in the URL and the 'Client-Cache' header is set accordingly to allow client-side caching, this ensures the latest version of the script is always loaded after having been modified.

```
<script type="text/javascript" src="/modules/CsrfServlet?tag=4301DD53426AC0B4A506226442AAB8F8"></script>
```

The client cache strategy can also be modified using the following property:

```
org.owasp.csrfguard.JavascriptServlet.cacheControlTagged = private, max-age=600
```
