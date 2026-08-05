import {addNode, createSite, deleteSite, publishAndWaitJobEnding} from '@jahia/cypress';
import {updateCsrfGuardFetchMetadataEnabled} from '../utils/utils';

describe('Fetch metadata request policy tests', () => {
    const targetSiteKey = 'csrfGuardSite';
    const actionUrl = '/en/sites/' + targetSiteKey + '/home.logAction.do';
    const pageUrl = '/en/sites/' + targetSiteKey + '/home.html';

    before('Create target test site', () => {
        cy.log('Create site ' + targetSiteKey + ' for csrf tests');
        createSite(targetSiteKey, {locale: 'en', templateSet: 'jahia-csrf-guard-test-module', serverName: 'localhost'});
        addNode({
            parentPathOrId: `/sites/${targetSiteKey}/home`,
            primaryNodeType: 'jnt:contentList',
            name: 'pagecontent'
        }).then(() => {
            addNode({
                parentPathOrId: `/sites/${targetSiteKey}/home/pagecontent`,
                primaryNodeType: 'csrf:testContent',
                name: 'test-content',
                mixins: ['jmix:renderable']
            }).then(() => {
                publishAndWaitJobEnding('/sites/' + targetSiteKey + '/home');
            });
        });
    });

    beforeEach(() => {
        cy.login();
    });

    afterEach(() => {
        cy.logout();
    });

    /**
     * Repeat the request until it answers the expected status: a configuration change reaches the module through its
     * configuration file, which is read at its own pace.
     */
    const crossSiteWriteEventuallyAnswers = (expectedStatus: number, remainingAttempts = 25) => {
        cy.request({
            method: 'POST',
            url: actionUrl,
            headers: {Origin: Cypress.config().baseUrl, 'Sec-Fetch-Site': 'cross-site'},
            failOnStatusCode: false
        }).then(response => {
            if (response.status === expectedStatus || remainingAttempts === 0) {
                expect(response.status).to.equal(expectedStatus);
            } else {
                // eslint-disable-next-line cypress/no-unnecessary-waiting
                cy.wait(2000);
                crossSiteWriteEventuallyAnswers(expectedStatus, remainingAttempts - 1);
            }
        });
    };

    it('should serve a write request reported as same-origin', () => {
        cy.request({
            method: 'POST',
            url: actionUrl,
            headers: {Origin: Cypress.config().baseUrl, 'Sec-Fetch-Site': 'same-origin'},
            failOnStatusCode: true
        }).its('status').should('equal', 200);
    });

    it('should serve a write request when the header is absent', () => {
        cy.request({
            method: 'POST',
            url: actionUrl,
            headers: {Origin: Cypress.config().baseUrl},
            failOnStatusCode: true
        }).its('status').should('equal', 200);
    });

    it('should reject a write request reported as cross-site', () => {
        cy.request({
            method: 'POST',
            url: actionUrl,
            headers: {Origin: Cypress.config().baseUrl, 'Sec-Fetch-Site': 'cross-site'},
            failOnStatusCode: false
        }).its('status').should('equal', 403);
    });

    it('should reject a cross-site request asking for a write method', () => {
        cy.request({
            method: 'GET',
            url: pageUrl + '?jcrMethodToCall=put',
            headers: {Origin: Cypress.config().baseUrl, 'Sec-Fetch-Site': 'cross-site'},
            failOnStatusCode: false
        }).its('status').should('equal', 403);
    });

    it('should serve a cross-site read request', () => {
        cy.request({
            method: 'GET',
            url: pageUrl,
            headers: {Origin: Cypress.config().baseUrl, 'Sec-Fetch-Site': 'cross-site'},
            failOnStatusCode: true
        }).its('status').should('equal', 200);
    });

    it('should follow the configuration switch', () => {
        updateCsrfGuardFetchMetadataEnabled(false);
        crossSiteWriteEventuallyAnswers(200);
        updateCsrfGuardFetchMetadataEnabled(true);
        crossSiteWriteEventuallyAnswers(403);
    });

    after('Clean', () => {
        updateCsrfGuardFetchMetadataEnabled(true);
        deleteSite(targetSiteKey);
    });
});
