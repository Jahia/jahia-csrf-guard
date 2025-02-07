import {addNode, createSite, deleteSite, publishAndWaitJobEnding} from '@jahia/cypress';

describe('Base CSRF tests', () => {
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

    it('should find csrf token in page action URLS', () => {
        cy.login();
        cy.log('The page should contains CSRF Tokens when accessed logged in');
        cy.visit('/en/sites/' + targetSiteKey + '/home.html');
        cy.get('head script[src="/modules/CsrfServlet"]').should('exist');
        cy.get('a#csrfLink')
            .should(link => {
                const href = link.attr('href');
                expect(href).to.contain('CSRFTOKEN');
            });
        // Doing the same as guest should not contain CSRF Tokens
        cy.logout();
        cy.log('The page should NOT contains CSRF Tokens when not logged');
        cy.visit('/en/sites/' + targetSiteKey + '/home.html');
        // Cy.get('head script[src="/modules/CsrfServlet"]').should('not.exist');
        cy.get('a#csrfLink')
            .should(link => {
                const href = link.attr('href');
                expect(href).to.not.contain('CSRFTOKEN');
            });
    });

    after('Clean', () => {
        deleteSite(targetSiteKey);
    });
});
