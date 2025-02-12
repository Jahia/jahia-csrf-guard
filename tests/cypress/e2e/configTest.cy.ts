import {addNode, createSite, deleteSite, publishAndWaitJobEnding} from '@jahia/cypress';
import {updateCsrfGuardWhiteListConfig} from '../utils/utils';

describe.skip('Config CSRF tests', () => {
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
        updateCsrfGuardWhiteListConfig('*.toto.do');
        cy.request({url: '/en/sites/' + targetSiteKey + '/home.logAction.do', failOnStatusCode: false}).its('status').should('equal', 404);
        updateCsrfGuardWhiteListConfig('*.logAction.do');
        cy.request({url: '/en/sites/' + targetSiteKey + '/home.logAction.do', failOnStatusCode: true}).its('status').should('equal', 200);
        cy.logout();
    });

    after('Clean', () => {
        updateCsrfGuardWhiteListConfig('*.logAction.do');
        deleteSite(targetSiteKey);
    });
});
