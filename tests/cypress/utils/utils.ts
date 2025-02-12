export const updateCsrfGuardWhiteListConfig = (whitelist?: string) => {
    if (!whitelist) {
        return;
    }

    const conf = {
        editConfiguration: 'org.jahia.modules.jahiacsrfguard-dummy',
        configIdentifier: 'global',
        properties: {
            whitelist: whitelist
        }
    };

    cy.runProvisioningScript({fileContent: JSON.stringify([conf]), type: 'application/json'}, null, {
        url: Cypress.env('JAHIA_PROCESSING_URL'),
        password: Cypress.env('SUPER_USER_PASSWORD'),
        username: 'root'
    });
    if (Cypress.env('JAHIA_CLUSTER_ENABLED')) {
        // Wait to allow to synchronize in cluster
        cy.wait(20000);
    } else {
        cy.wait(10000);
    }
};

export const updateCsrfGuardBypassGuest = (bypass: boolean) => {
    const conf = {
        editConfiguration: 'org.jahia.modules.jahiacsrfguard.global',
        configIdentifier: 'global',
        properties: {
            'jahia.csrf-guard.bypassForGuest': bypass
        }
    };

    cy.runProvisioningScript({fileContent: JSON.stringify([conf]), type: 'application/json'}, null, {
        url: Cypress.env('JAHIA_PROCESSING_URL'),
        password: Cypress.env('SUPER_USER_PASSWORD'),
        username: 'root'
    });
    if (Cypress.env('JAHIA_CLUSTER_ENABLED')) {
        // Wait to allow to synchronize in cluster
        cy.wait(20000);
    } else {
        cy.wait(10000);
    }
};
