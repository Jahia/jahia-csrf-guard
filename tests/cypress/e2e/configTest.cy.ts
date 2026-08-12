import {addNode, createSite, deleteSite, publishAndWaitJobEnding} from '@jahia/cypress';
import {updateCsrfGuardWhiteListConfig} from '../utils/utils';

describe('Config CSRF tests', () => {
    const targetSiteKey = 'csrfGuardSite';
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

    it('should be removed from the whitelist', () => {
        cy.login();
        cy.request({method: 'POST', url: '/en/sites/' + targetSiteKey + '/home.logAction.do', failOnStatusCode: true}).its('status').should('equal', 200);
        updateCsrfGuardWhiteListConfig('*.toto.do');
        cy.request({method: 'POST', url: '/en/sites/' + targetSiteKey + '/home.logAction.do', failOnStatusCode: false}).its('status').should('equal', 400);
        updateCsrfGuardWhiteListConfig('*.logAction.do');
        cy.request({method: 'POST', url: '/en/sites/' + targetSiteKey + '/home.logAction.do', failOnStatusCode: true}).its('status').should('equal', 200);
        cy.logout();
    });

    // Jahia serves /home.logAction.do/ as the action /home.logAction.do — its resolver drops the trailing slash — so
    // a configuration that names that action must decide the same way for both forms of its url.
    it('should decide on a url that only differs by a trailing slash the same way', () => {
        cy.login();
        const actionUrl = '/en/sites/' + targetSiteKey + '/home.logAction.do';
        // Smoke check — the action answers on both forms of its url. Both are served on any build, so this pair
        // does not discriminate; the 400s below are the assertion that does.
        cy.request({method: 'POST', url: actionUrl, failOnStatusCode: true}).its('status').should('equal', 200);
        cy.request({method: 'POST', url: actionUrl + '/', failOnStatusCode: true}).its('status').should('equal', 200);
        updateCsrfGuardWhiteListConfig('*.toto.do');
        // Covered by token validation: the plain form answers 400, which is what makes the next line meaningful
        cy.request({method: 'POST', url: actionUrl, failOnStatusCode: false}).its('status').should('equal', 400);
        cy.request({method: 'POST', url: actionUrl + '/', failOnStatusCode: false}).its('status').should('equal', 400);
        updateCsrfGuardWhiteListConfig('*.logAction.do');
        cy.logout();
    });

    after('Clean', () => {
        updateCsrfGuardWhiteListConfig('*.logAction.do');
        deleteSite(targetSiteKey);
    });
});
