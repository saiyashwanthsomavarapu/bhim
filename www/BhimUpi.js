var exec = require('cordova/exec');

exports.startTranascation = function (arg0, success, error) {
    exec(success, error, 'BhimUpi', 'startTranascation', [arg0]);
};
