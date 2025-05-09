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

    cy.runProvisioningScript({fileContent: JSON.stringify([conf]), type: 'application/json'}, null);
    if (Cypress.env('JAHIA_CLUSTER_ENABLED')) {
        // Wait to allow to synchronize in cluster
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(20000);
    } else {
        // eslint-disable-next-line cypress/no-unnecessary-waiting
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

    cy.runProvisioningScript({fileContent: JSON.stringify([conf]), type: 'application/json'}, null);
    if (Cypress.env('JAHIA_CLUSTER_ENABLED')) {
        // Wait to allow to synchronize in cluster
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(20000);
    } else {
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(10000);
    }
};
