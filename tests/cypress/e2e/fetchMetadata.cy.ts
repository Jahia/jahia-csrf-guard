import {addNode, createSite, deleteSite, publishAndWaitJobEnding} from '@jahia/cypress';
import {updateCsrfGuardFetchMetadataWhiteList} from '../utils/utils';

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

    // The policy keys on the request shape, not on what the request writes: a content-creation request
    // through the render pipeline is served or refused on the same terms as any other write, whatever
    // node type it carries.
    const createChildUrl = '/cms/render/default/en/sites/' + targetSiteKey + '/home/pagecontent';

    it('should serve a content-creation request reported as same-origin', () => {
        cy.request({
            method: 'POST',
            url: createChildUrl,
            form: true,
            body: {jcrNodeType: 'jnt:contentList', nodeName: 'sameOriginChild'},
            headers: {Origin: Cypress.config().baseUrl, 'Sec-Fetch-Site': 'same-origin'},
            followRedirect: false,
            failOnStatusCode: false
        }).its('status').should('be.oneOf', [200, 201, 303]);
    });

    it('should reject a cross-site content-creation request whatever node type it carries', () => {
        cy.request({
            method: 'POST',
            url: createChildUrl,
            form: true,
            body: {jcrNodeType: 'jnt:contentList', nodeName: 'crossSiteChild'},
            headers: {Origin: Cypress.config().baseUrl, 'Sec-Fetch-Site': 'cross-site'},
            followRedirect: false,
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

    it('should exempt a whitelisted url and cover it again once removed', () => {
        updateCsrfGuardFetchMetadataWhiteList('*.logAction.do');
        crossSiteWriteEventuallyAnswers(200);
        updateCsrfGuardFetchMetadataWhiteList('*.notAnAction.do');
        crossSiteWriteEventuallyAnswers(403);
    });

    after('Clean', () => {
        updateCsrfGuardFetchMetadataWhiteList('*.notAnAction.do');
        deleteSite(targetSiteKey);
    });
});
