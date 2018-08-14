var bleno = require('bleno');
var os = require('os');
var util = require('util');

var BlenoCharacteristic = bleno.Characteristic;

const constants = require('../constants');

// Hot (pin 4)
class TempOneCharacteristic {
    constructor() {
        TempOneCharacteristic.super_.call(this, {
            uuid: constants.TEMP1_UUID,
            properties: ['read'],
        });
        this._value = new Buffer(0);
    }
    
    onReadRequest(offset, callback) {
        if (!offset) {
            // DHT11
            sensor.read(11, constants.HOT_PIN, function (err, temperature, humidity) {
                if (!err) {
                    console.log('HOT - temp: ' + temperature.toFixed(1) + '°C, ' +
                        'humidity: ' + humidity.toFixed(1) + '%');
                    this._value = new Buffer(JSON.stringify({
                        'temp': temp
                    }));
                    callback(this.RESULT_SUCCESS, this._value.slice(offset, this._value.length));
                }
            });
        }
        console.log('TempOneCharacteristic - onReadRequest: value = ' +
            this._value.slice(offset, offset + bleno.mtu).toString());
        if (offset) {
            callback(this.RESULT_SUCCESS, this._value.slice(offset, this._value.length));
        }
    }
}


util.inherits(TempOneCharacteristic, BlenoCharacteristic);
module.exports = TempOneCharacteristic;
