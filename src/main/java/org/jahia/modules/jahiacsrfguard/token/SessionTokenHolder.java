package org.jahia.modules.jahiacsrfguard.token;

import org.apache.commons.lang3.tuple.Pair;
import org.owasp.csrfguard.token.TokenUtils;
import org.owasp.csrfguard.token.storage.Token;
import org.owasp.csrfguard.token.storage.TokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class SessionTokenHolder implements TokenHolder {
    private static final Logger logger = LoggerFactory.getLogger(SessionTokenHolder.class);
    public static final String CSRF_TOKEN = "CSRF_TOKEN";

    private static ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<>();

    public SessionTokenHolder() {}

    @Override
    public void setMasterToken(String sessionKey, String value) {
        Token token = getToken(sessionKey);
        if (token == null) {
            token = new SerializableToken(value);
        } else {
            token.setMasterToken(value);
        }
        saveToken(sessionKey, token);
    }

    @Override
    public String createMasterTokenIfAbsent(String sessionKey, Supplier<String> valueSupplier) {
        Token token = getToken(sessionKey);
        if (token == null) {
            token = createToken(sessionKey, valueSupplier);
        }
        return token.getMasterToken();
    }

    @Override
    public String createPageTokenIfAbsent(String sessionKey, String resourceUri, Supplier<String> valueSupplier) {
        Token token = getToken(sessionKey);
        String pageToken;
        if (Objects.isNull(token)) {
            pageToken = valueSupplier.get();
            token = new SerializableToken(valueSupplier.get(), Pair.of(resourceUri, pageToken));
        } else {
            pageToken = token.setPageTokenIfAbsent(resourceUri, valueSupplier);
        }

        saveToken(sessionKey, token);
        return pageToken;
    }

    @Override
    public Token getToken(String sessionKey) {
        Token token = null;
        HttpSession session = getSession(sessionKey);
        if (session != null) {
            try {
                token = (Token) session.getAttribute(CSRF_TOKEN);
            } catch (ClassCastException e) {
                logger.debug("Invalid class for token, reset to new one");
            }
            if (token == null) {
                token = createToken(sessionKey, TokenUtils::generateRandomToken);
            }
        }
        return token;
    }

    @Override
    public String getPageToken(String sessionKey, String resourceUri) {
        Token token = getToken(sessionKey);
        return Objects.nonNull(token) ? token.getPageToken(resourceUri) : null;
    }

    @Override
    public void setPageToken(String sessionKey, String resourceUri, String value) {
        Token token = getTokenOrException(sessionKey);
        token.setPageToken(resourceUri, value);
        saveToken(sessionKey, token);
    }

    @Override
    public void setPageTokens(String sessionKey, Map<String, String> pageTokens) {
        Token token = getTokenOrException(sessionKey);
        token.setPageTokens(pageTokens);
        saveToken(sessionKey, token);
    }

    @Override
    public Map<String, String> getPageTokens(String sessionKey) {
        return getTokenOrException(sessionKey).getPageTokens();
    }

    @Override
    public void remove(String sessionKey) {
    }

    @Override
    public void rotateAllPageTokens(String sessionKey, Supplier<String> tokenValueSupplier) {
        final Token token = getTokenOrException(sessionKey);
        token.rotateAllPageTokens(tokenValueSupplier);
        saveToken(sessionKey, token);
    }

    @Override
    public void regenerateUsedPageToken(String sessionKey, String tokenFromRequest, Supplier<String> tokenValueSupplier) {
        final Token token = getTokenOrException(sessionKey);
        token.regenerateUsedPageToken(tokenFromRequest, tokenValueSupplier);
        saveToken(sessionKey, token);
    }

    private Token getTokenOrException(final String sessionKey) {
        Token token = getToken(sessionKey);

        if (Objects.isNull(token)) {
            throw new IllegalStateException("Token with the provided session key does not exist!");
        } else {
            return token;
        }
    }

    private Token createToken(String sessionKey, Supplier<String> valueSupplier) {
        Token token;
        token = new SerializableToken(valueSupplier.get());
        saveToken(sessionKey, token);
        return token;
    }

    private void saveToken(final String sessionKey, Token token) {
        HttpSession session = getSession(sessionKey);
        if (session != null) {
            session.setAttribute(CSRF_TOKEN, token);
        }
    }

    public static void setCurrentRequest(HttpServletRequest request) {
        currentRequest.set(request);
    }

    private static HttpSession getSession(String sessionKey) {
        HttpSession session = currentRequest.get() != null ? currentRequest.get().getSession() : null;
        if (session != null && !session.getId().equals(sessionKey)) {
            logger.error("Session id does not match");
            return null;
        }
        return session;
    }

}
