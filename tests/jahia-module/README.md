# Dummy CSRF Test Module

This module contains 2 Actions:
* LogAction -> output a message in the console
* UpdateTextAction -> Update the text of a node

To use the UpdateTextAction add the component `Dummy CSRF Action Caller` to a page in edit, you will see 2 buttons and text when clicking on it, it will update the text displayed, it's working in preview mode only (no live).  

To use the LogAction:
* add the following form in a simple HTML page
```html
<form method="GET" action="http://localhost:8080/cms/render/default/en/sites.logAction.do">
    <button type="submit">
        CALL LogAction
    </button>
</form>
```
* if you prefer you can add a simple script with jQuery
```javascript

$.post('http://localhost:8080/cms/render/default/en/sites.logAction.do', function (result) {
    console.log(result);
}, 'json');
```
* Open the HTML page in a browser where you are authenticated
* Click on the button if you used the HTML form
* Check your log you should see the message: `****************************************** Hello Jahia!`

###### Why do you see this log?  
Because the LogAction is whitelisted by default with this module. You can change it by modifying the file `/path/to/your/instance/digital-factory-data/karaf/etc/org.jahia.modules.jahiacsrfguard-dummy.cfg` in the file just replace `whitelist = *.logAction.do` by `whitelist = *.toto.do`
