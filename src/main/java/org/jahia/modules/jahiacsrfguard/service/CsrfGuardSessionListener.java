/*
 * Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.modules.jahiacsrfguard.service;

/**
 * This class is only used to be able to disable the internal listener on HTTP sessions
 * It's useful when the sessions are manage by a third party module and the third party module want to provide his own session management and listener.
 */
public interface CsrfGuardSessionListener {

    /**
     * Disable or enable the Csrf guard http session listener
     * @param enabled true or false
     */
    void setEnabled(boolean enabled);
}
