import {waitUntilSAMStatusGreen} from '@jahia/cypress';

describe('Absence of errors in SAM', () => {
    it('Wait until SAM returns GREEN for low severity - CRITICAL', () => {
        // The timeout of 3mn (180s) is there to allow for the cluster to finish its synchronization
        waitUntilSAMStatusGreen('LOW', 180000);
    });
});
