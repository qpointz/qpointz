const CracoLessPlugin = require('craco-less-plugin');
module.exports = {
    plugins: [
        {
            plugin: CracoLessPlugin,
            options: {
                modifyVars: {},
                javascriptEnabled: true,
            },
        },
    ],
};

