var bleno = require('bleno');
var os = require('os');
var util = require('util');

var BlenoCharacteristic = bleno.Characteristic;

const constants = require('../constants')

var LedCharacteristic = function() {

 LedCharacteristic.super_.call(this, {
    uuid: constants.LED_UUID,
    properties: ['read'],
  });

 this._value = new Buffer(0);
};

LedCharacteristic.prototype.onReadRequest = function(offset, callback) {

  if(!offset) {

    this._value = new Buffer(JSON.stringify({
      'uptime' : os.uptime()
    }));
  }

  console.log('LedCharacteristic - onReadRequest: value = ' +
    this._value.slice(offset, offset + bleno.mtu).toString()
  );

  callback(this.RESULT_SUCCESS, this._value.slice(offset, this._value.length));
};

util.inherits(LedCharacteristic, BlenoCharacteristic);
module.exports = LedCharacteristic;
