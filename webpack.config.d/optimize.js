(function () {
    var path = require('path');

    config.entry = [path.resolve(__dirname, "kotlin-dce/SpaceshipWars.js")];

    config.resolve.modules.unshift(path.resolve(__dirname, "kotlin-dce"));

    config.mode = "production";

    config.devtool = "source-map"
})();