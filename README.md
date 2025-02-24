# Jahia CSRF Guard

This module will add CSRF token protection on all call to a Jahia Action.
It's based on [OWASP CSRFGuard library](https://owasp.org/www-project-csrfguard/). 

The module act as a Wrapper over the OWASP library. It integrates with Jahia using 2 Servlet Filters : 
 - CsrfGuardFilter : This filter will inject a <script> tag in relevant responses (HTML) to add a reference to CSRF javascript file.
 - CsrfGuardServletFilterWrapper: This filter will introduce a configurable pattern allowing some URLs to be protected or not by CSRF Filter
All the configuration and registration of the OWASP library is done in the module using OSGI configuration.
Two level of configuration are possible, a global level providing options for the whole wrapper and a per module configuration that manage fine grain
configuration patterns.
The original JavaScriptServlet is registered at startup by the module and will serve the CSRF javascript file.
All original OWASP library configuration options can be override using OSGI configuration.

### How to upgrade OWASP CSRFGuard

#### Minor version
Upgrade to a minor version should be seamless but you will have to compare the csrfguard.template.js with its original version to merge the changes.

#### Major version
It will depend of the changelog and breaking changes of the version, but again here do not forget to compare the csrfguard.template.js with its original version to merge the changes.

#### Minification

Minify the template using the following command :

```terser -m -c conditionals=false -f quote_style=1 -- src/main/resources/META-INF/csrfguard.template.uncompressed.js```

## Open-Source

This is an Open-Source module, you can find more details about Open-Source @ Jahia [in this repository](https://github.com/Jahia/open-source).
