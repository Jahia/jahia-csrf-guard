var owaspCSRFGuardScriptHasLoaded=owaspCSRFGuardScriptHasLoaded||{};if(!0!==owaspCSRFGuardScriptHasLoaded)!function(){if(owaspCSRFGuardScriptHasLoaded=!0,!window.csrfguarded){var e,t={listEvents:e=[],add:function(t,n,s){e.push(arguments)},flush:function(){var t,n;for(t=e.length-1;t>=0;t-=1){if((n=e[t])[0].removeEventListener)n[0].removeEventListener(n[1],n[2],n[3]);if('on'!==n[1].substring(0,2))n[1]='on'+n[1];if(n[0].detachEvent)n[0].detachEvent(n[1],n[2])}}},n='%DOMAIN_ORIGIN%'.split(',');if(f(document.domain,n)){var s='%TOKEN_NAME%',o='%TOKEN_VALUE%';console.debug('Master token ['+s+']: ',o);var i={isDomContentLoaded:!1},r={pageTokens:{}};if(a(window,'unload',t.flush),a(window,'DOMContentLoaded',(function(){if(i.isDomContentLoaded=!0,r.pageTokensLoaded)T(s,o,r.pageTokens)})),'%INJECT_DYNAMIC_NODES%')!function(){const e='%DYNAMIC_NODE_CREATION_EVENT_NAME%';if(e)a(window,e,(function(e){w([e.detail],s,o,r.pageTokens)}));else if(MutationObserver){const e=new MutationObserver((function(e,t){for(let t in e){const n=e[t],i=n.addedNodes;if('childList'===n.type&&i.length&&i.length>0)w(i,s,o,r.pageTokens)}}));e.observe(document,{attributes:!1,childList:!0,subtree:!0}),a(window,'unload',e.disconnect)}else a(window,'DOMNodeInserted',(function(e){const t=e.target||e.srcElement;if('DOMNodeInserted'===e.type)w([t],s,o,r.pageTokens)}))}();if('%INJECT_XHR%'){if('Microsoft Internet Explorer'===navigator.appName)!function(){var e=window.XMLHttpRequest;function t(){this.base=e?new e:new window.ActiveXObject('Microsoft.XMLHTTP')}function n(){return new t}n.prototype=t.prototype,n.UNSENT=0,n.OPENED=1,n.HEADERS_RECEIVED=2,n.LOADING=3,n.DONE=4,n.prototype.status=0,n.prototype.statusText='',n.prototype.readyState=n.UNSENT,n.prototype.responseText='',n.prototype.responseXML=null,n.prototype.onsend=null,n.url=null,n.onreadystatechange=null,n.prototype.open=function(e,t,n,s,o){var i=this;this.url=t,this.base.onreadystatechange=function(){try{i.status=i.base.status}catch(e){}try{i.statusText=i.base.statusText}catch(e){}try{i.readyState=i.base.readyState}catch(e){}try{i.responseText=i.base.responseText}catch(e){}try{i.responseXML=i.base.responseXML}catch(e){}if(null!==i.onreadystatechange)i.onreadystatechange.apply(this,arguments)},this.base.open(e,t,n,s,o)},n.prototype.send=function(e){if(null!==this.onsend)this.onsend.apply(this,arguments);this.base.send(e)},n.prototype.abort=function(){this.base.abort()},n.prototype.getAllResponseHeaders=function(){return this.base.getAllResponseHeaders()},n.prototype.getResponseHeader=function(e){return this.base.getResponseHeader(e)},n.prototype.setRequestHeader=function(e,t){return this.base.setRequestHeader(e,t)},window.XMLHttpRequest=n}();else XMLHttpRequest.prototype._open=XMLHttpRequest.prototype.open,XMLHttpRequest.prototype.open=function(e,t,n,s,o){this.url=t,this._open.apply(this,arguments)},XMLHttpRequest.prototype._send=XMLHttpRequest.prototype.send,XMLHttpRequest.prototype.send=function(e){if(null!==this.onsend)this.onsend.apply(this,arguments);this._send.apply(this,arguments)};XMLHttpRequest.prototype.onsend=function(e){a(this,'readystatechange',(function(){if(4===this.readyState)if(-1!==this.getAllResponseHeaders().indexOf(s.toLowerCase())){let e=this.getResponseHeader(s);try{let t=JSON.parse(e),n=t.masterToken;if(void 0!==n)o=n,console.debug('New master token value received: ',o);let i=t.pageTokens;if(void 0!==i)Object.keys(i).forEach((function(e){return r.pageTokens[e]=i[e]})),console.debug('New page token value(s) received: ',i);T(s,o,r.pageTokens)}catch(e){console.error('Error while updating tokens from response header.')}}}));if(c(this.url)&&d(this.url)){this.setRequestHeader('X-Requested-With','XMLHttpRequest');let e=function(e){var t=function(e,t){let n=e.indexOf(t);return n>0?e.substring(0,n):e};let n=u(e,'/')?e:'/'+e;return n=t(n,'?'),n=t(n,'#'),n}(this.url);if(null===r.pageTokens)this.setRequestHeader(s,o);else{let t=h(r.pageTokens,e);if(null==t){let t=function(e,t){let n=null,s=window.location.pathname.substring(1).split('/'),o='';for(let i=0;i<s.length-1;i++){o+='/'+s[i];let r=h(e,o+t);if(null!=r){n=r;break}}return n}(r.pageTokens,e);if(null===t)this.setRequestHeader(s,o);else this.setRequestHeader(s,t)}else this.setRequestHeader(s,t)}}}}if('%TOKENS_PER_PAGE%'){!function(e,t,n){const s=window.XMLHttpRequest?new window.XMLHttpRequest:new window.ActiveXObject('Microsoft.XMLHTTP');if(s.open('POST','%SERVLET_PATH%','%ASYNC_XHR%'),1)if(void 0!==e&&void 0!==t)s.setRequestHeader(e,t);s.onreadystatechange=function(){if(4===s.readyState)if(200===s.status){let e=JSON.parse(s.responseText).pageTokens;console.debug('Received page tokens: ',e),n.call(this,e)}else if(0===s.status)console.warn('CSRF page tokens response is empty (may be due to a aborted request).');else console.error('CSRF check failed, request status: {}',s.status)},s.send(null)}(s,o,(function(e){if(r.pageTokens=e,r.pageTokensLoaded=!0,i.isDomContentLoaded)T(s,o,e)}))}else a(window,'DOMContentLoaded',(function(){T(s,o,{})}));window.csrfguarded=!0}else console.error('OWASP CSRFGuard JavaScript was included from within an unauthorized domain!')}function a(e,n,s){if(e.addEventListener)e.addEventListener(n,s,!1),t.add(e,n,s);else if(e.attachEvent)e['e'+n+s]=s,e[n+s]=function(){e['e'+n+s](window.event)},e.attachEvent('on'+n,e[n+s]),t.add(e,n,s);else e['on'+n]=e['e'+n+s]}function u(e,t){return 0===e.indexOf(t)}function l(e,t){return e.substring(e.length-t.length)===t}function d(e){let t=-1!==e.indexOf('?')?e.substring(0,e.indexOf('?')):e;return l(t,'.do')||l(t,'/*')}function f(e,t){var n=!1;if(t&&t.constructor===Array){for(var s=0;s<t.length;s++)if(f(e,t[s]))return!0;return!1}if(e===t)n=!0;else if(!1===Boolean('%DOMAIN_STRICT%'))if('.'===t.charAt(0))n=l(e,t);else n=l(e,'.'+t);return n}function c(e){var t=!1;if('http://'===e.substring(0,7)||'https://'===e.substring(0,8)){for(var n=e.indexOf('://'),s=e.substring(n+3),o='',i=0;i<s.length;i++){var r=s.charAt(i);if('/'===r||':'===r||'#'===r)break;else o+=r}t=f(document.domain,o)}else if('#'===e.charAt(0))t=!1;else if(!u(e,'//')&&('/'===e.charAt(0)||-1===e.search(/^[a-zA-Z][a-zA-Z0-9.+-]*:/)))t=!0;return t}function p(e){var t='',n=e.indexOf('://'),s='';if(n>0)s=e.substring(n+3);else if('/'!==e.charAt(0))s='%CONTEXT_PATH%/'+e;else s=e;for(var o=-1===n,i=0;i<s.length;i++){var r=s.charAt(i);if('/'===r)o=!0;else if(!0===o&&('?'===r||'#'===r)){o=!1;break}if(!0===o)t+=r}return t}function h(e,t){let n=null;return Object.keys(e).forEach((function(s){var o=e[s];if(t===s)n=o;else if(u(s,'^')&&l(s,'$')){if(new RegExp(s).test(t))n=o}else if(u(s,'/*'))n=o;else if(u(t,'%CONTEXT_PATH%')&&l(s,t.substring(14)))n=o;else if(l(s,'/*')||u(s,'.*'))console.warn('\'Extension\' and \'partial path wildcard\' matching for page tokens is not supported properly yet! Every resource will be assigned a new unique token instead of using the defined resource matcher token. Although this is not a security issue, in case of a large REST application it can have an impact on performance.Consider using regular expressions instead.')})),n}function g(e,t,n,s,o){if(!o){var i=e.getAttribute('method');if(null!=i&&'get'===i.toLowerCase())return}var r=n,a=e.getAttribute('action');if(null!==a&&c(a)&&d(a)){const o=h(s,p(a));r=null==o?n:o;let i=Object.keys(e.elements).filter((function(n){return e.elements[n].name===t}));if(0===i.length){var u=document.createElement('input');u.setAttribute('type','hidden'),u.setAttribute('name',t),u.setAttribute('value',r),e.appendChild(u),console.debug('Hidden input element [',u,'] was added to the form: ',e)}else i.forEach((function(t){return e.elements[t].value=r})),console.debug('Hidden token fields [',i,'] of form [',e,'] were updated with new token value: ',r)}}function E(e,t,n,s,o){const i=function(e,t,n){let s;if(-1===e.indexOf('?'))s=e+'?'+t+'='+n;else s=e+'&'+t+'='+n;return s},r=e.getAttribute&&e.getAttribute(t);if(null!=r&&c(r)&&d(r)&&!function(e){var t=!1,n='%UNPROTECTED_EXTENSIONS%';if(''!==n)for(var s=function(e){var t='';if(-1!==e.indexOf(';'))e=e.split(';')[0];if(-1!==e.indexOf('.'))t=e.substring(e.lastIndexOf('.')+1,e.length)||e;return t}(p(e)).toLowerCase(),o=n.split(','),i=0;i<o.length;i++)if(o[i]===s){t=!0;break}return t}(r)){const a=h(o,p(r)),u=null==a?s:a,l=new RegExp('(?:'+n+'=)([^?|#|&]+)','gi').exec(r);if(null===l||0===l.length){let s;const o=r.indexOf('#');if(-1!==o){const e=r.split('#')[0],t=r.substring(o);s=i(e,n,u)+t}else s=i(r,n,u);try{e.setAttribute(t,s),console.debug('Attribute [',t,'] with value [',s,'] set for element: ',e)}catch(e){}}else{let n=r;l.slice(1).forEach((function(e){return n=n.replace(e,u)})),e.setAttribute(t,n),console.debug('Attribute [',t,'] with value [',n,'] set for element: ',e)}}}function w(e,t,n,s){var o=e.length;for(let i=0;i<o;i++){let r=e[i];if(r.tagName&&'form'===r.tagName.toLowerCase()){if('%INJECT_FORMS%')g(r,t,n,s,'%INJECT_GET_FORMS%'),o=e.length;if('%INJECT_FORM_ATTRIBUTES%')E(r,'action',t,n,s)}else if('%INJECT_ATTRIBUTES%')E(r,'src',t,n,s),E(r,'href',t,n,s)}}function T(e,t,n){var s={};if('%TOKENS_PER_PAGE%')s=n;w(document.all?document.all:document.getElementsByTagName('*'),e,t,s)}}();
