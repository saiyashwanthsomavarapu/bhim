var exec = require('cordova/exec');

module.exports.startTranascation = function (arg0, success, error) {
    exec(success, error, "BhimUpi", "startTranascation", [arg0]);
};

module.exports.getAllUpiApps = function(success,error) {
    exec(success,error,"apps","getAllUpiApps");
}