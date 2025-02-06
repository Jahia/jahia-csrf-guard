/* eslint-disable @typescript-eslint/no-var-requires */
const sshCommand = require('./ssh')
module.exports = (on, config) => {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    require('@jahia/cypress/dist/plugins/registerPlugins').registerPlugins(on, config);
    // Custom tasks (Useful to run code in Node from cypress tests)
    on('task', {
        sshCommand(commands) {
            return sshCommand(commands, {
                hostname: process.env.JAHIA_HOST,
                port: process.env.JAHIA_PORT_KARAF,
                username: process.env.JAHIA_USERNAME_TOOLS,
                password: process.env.JAHIA_PASSWORD_TOOLS
            });
        }
    });
    return config;
};
