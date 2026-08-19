import {addNode, createSite, deleteSite, publishAndWaitJobEnding} from '@jahia/cypress';

// The module exempts one URL from the fetch metadata request policy on its own account: the SAML callback, the
// endpoint an identity provider posts its assertion back to. The other URLs that SAML support serves — connect,
// metadata — are read requests this policy does not cover on their own account, so a cross-site WRITE to any of
// them must still be rejected: only the callback is exempt.
describe('Fetch metadata request policy — built-in SAML exemption', () => {
    const targetSiteKey = 'csrfGuardSite';
    const crossSiteHeaders = {Origin: Cypress.config().baseUrl, 'Sec-Fetch-Site': 'cross-site'};

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

    const nonCallbackSamlWrites = [
        {label: 'the plain SAML suffix', url: `/en/sites/${targetSiteKey}/home.saml`},
        {label: 'the connect endpoint', url: `/en/sites/${targetSiteKey}/home.connect.saml`},
        {label: 'the metadata endpoint', url: `/en/sites/${targetSiteKey}/home.metadata.saml`}
    ];

    nonCallbackSamlWrites.forEach(({label, url}) => {
        it(`should reject a cross-site write to ${label}`, () => {
            cy.request({method: 'POST', url, headers: crossSiteHeaders, failOnStatusCode: false})
                .its('status').should('equal', 403);
        });
    });

    it('should reject a cross-site write to a non-callback SAML url asked for through the query string', () => {
        cy.request({
            method: 'GET',
            url: `/en/sites/${targetSiteKey}/home.saml?jcrMethodToCall=put`,
            headers: crossSiteHeaders,
            failOnStatusCode: false
        }).its('status').should('equal', 403);
    });

    it('should not reject a cross-site write to the SAML callback url', () => {
        cy.request({method: 'POST', url: `/en/sites/${targetSiteKey}/home.callback.saml`, headers: crossSiteHeaders, failOnStatusCode: false})
            .its('status').should('not.equal', 403);
    });

    after('Clean', () => {
        deleteSite(targetSiteKey);
    });
});
