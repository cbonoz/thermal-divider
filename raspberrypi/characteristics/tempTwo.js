var bleno = require('bleno');
var os = require('os');
var util = require('util');

var BlenoCharacteristic = bleno.Characteristic;

const constants = require('../constants');

var TempTwoCharacteristic = function() {
 TempTwoCharacteristic.super_.call(this, {
    uuid: constants.TEMP2_UUID,
    properties: ['read'],
  });

 this._value = new Buffer(0);
};

TempTwoCharacteristic.prototype.onReadRequest = function(offset, callback) {

  if(!offset) {
   
    // TODO: get temperature.
    const temp = 5;

    this._value = new Buffer(JSON.stringify({
      'temperature' : temp
    }));
  }

  console.log('TempTwoCharacteristic - onReadRequest: value = ' +
    this._value.slice(offset, offset + bleno.mtu).toString()
  );

  callback(this.RESULT_SUCCESS, this._value.slice(offset, this._value.length));
};

util.inherits(TempTwoCharacteristic, BlenoCharacteristic);
module.exports = TempTwoCharacteristic;