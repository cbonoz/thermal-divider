var bleno = require('bleno');
var os = require('os');
var util = require('util');

var BlenoCharacteristic = bleno.Characteristic;

const constants = require('../constants')

var SwitchCharacteristic = function() {
 SwitchCharacteristic.super_.call(this, {
    uuid: constants.SWITCH_UUID,
    properties: ['read'],
  });

 this._value = new Buffer(0);
};

SwitchCharacteristic.prototype.onReadRequest = function(offset, callback) {

  if(!offset) {

    var loadAverage = os.loadavg().map(function(currentValue, index, array){

      return currentValue.toFixed(3);
    });

    this._value = new Buffer(JSON.stringify({
      'oneMin' : loadAverage[0],
      'fiveMin': loadAverage[1],
      'fifteenMin': loadAverage[2]
    }));
  }

  console.log('SwitchCharacteristic - onReadRequest: value = ' +
    this._value.slice(offset, offset + bleno.mtu).toString()
  );

  callback(this.RESULT_SUCCESS, this._value.slice(offset, this._value.length));
};

util.inherits(SwitchCharacteristic, BlenoCharacteristic);
module.exports = SwitchCharacteristic;
