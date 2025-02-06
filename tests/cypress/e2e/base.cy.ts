import {addNode, createSite, deleteSite, publishAndWaitJobEnding} from "@jahia/cypress";

describe('Base CSRF tests', () => {
    const targetSiteKey = 'csrfGuardSite';
    before('Create target test site', () => {
        cy.log('Create site ' + targetSiteKey + ' for csrf tests');
        createSite(targetSiteKey, {locale: 'en', templateSet: 'jahia-csrf-guard-test-module', serverName: 'localhost'});
        addNode({parentPathOrId: `/sites/${targetSiteKey}/home`,
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
        cy.request({
            url: '/en/sites/' + targetSiteKey + '/home.html',
            followRedirect: true,
            failOnStatusCode: false
        }).then(response => {
            expect(response.status).to.eq(200);
            expect(response.body).to.contain('bodywrapper');
        });
        cy.logout();
        cy.log('The page should NOT contains CSRF Tokens when not logged');
        cy.request({
            url: '/en/sites/' + targetSiteKey + '/home.html',
            followRedirect: true,
            failOnStatusCode: false
        }).then(response => {
            expect(response.status).to.eq(200);
            expect(response.body).to.contain('bodywrapper');
        });
    });

    after('Clean', () => {
        deleteSite(targetSiteKey);
    });

});
