var bleno = require('bleno');
var os = require('os');
var util = require('util');
var rpio = require('rpio');

var BlenoCharacteristic = bleno.Characteristic;

const constants = require('../constants')

class SwitchCharacteristic {
  constructor() {
    SwitchCharacteristic.super_.call(this, {
      uuid: constants.SWITCH_UUID,
      properties: ['write', 'read'],
    });
    this._value = new Buffer(0);
  }
  
  onWriteRequest(data, offset, withoutResponse, callback) {
    this._value = data;
    const val = this._value.toString('hex');
    console.log('SwitchCharacteristic - onWriteRequest: value = ' + val);
    if (this._updateValueCallback) {
      console.log('SwitchCharacteristic - onWriteRequest: notifying');
      const newVal = (parseInt(val) == 1) ? rpio.HIGH : rpio.LOW;
      rpio.write(constants.RELAY_PIN, newVal);
      rpio.write(constants.LED_PIN, newVal);
      this._updateValueCallback(this._value);
    }
    callback(this.RESULT_SUCCESS);
  }

  onReadRequest(offset, callback) {
    if (!offset) {
      const val = rpio.write(constants.RELAY_PIN);
      console.log('read switch', val);
      this._value = new Buffer(JSON.stringify({
        'status': val
      }));
    }
    console.log('SwitchCharacteristic - onReadRequest: value = ' +
      this._value.slice(offset, offset + bleno.mtu).toString());
    callback(this.RESULT_SUCCESS, this._value.slice(offset, this._value.length));
  }
}


util.inherits(SwitchCharacteristic, BlenoCharacteristic);
module.exports = SwitchCharacteristic;
