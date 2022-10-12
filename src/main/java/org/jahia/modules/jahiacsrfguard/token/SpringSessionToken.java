package org.jahia.modules.jahiacsrfguard.token;

import org.apache.commons.lang3.tuple.Pair;
import org.owasp.csrfguard.token.storage.impl.InMemoryToken;

import java.io.Serializable;

public class SpringSessionToken extends InMemoryToken implements Serializable {
    public SpringSessionToken(String masterToken) {
        super(masterToken);
    }

    public SpringSessionToken(String masterToken, Pair<String, String> pageToken) {
        super(masterToken, pageToken);
    }
}
