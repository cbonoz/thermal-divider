var bleno = require('bleno');
var util = require('util');

var TempOneCharacteristic = require('./characteristics/tempOne');
var TempTwoCharacteristic = require('./characteristics/tempTwo');
var SwitchCharacteristic = require('./characteristics/switch');
// var LedCharacteristic = require('./characteristics/led');

function SystemInformationService() {

  bleno.PrimaryService.call(this, {
    uuid: 'ff51b30e-d7e2-4d93-8842-a7c4a57dfb07',
    characteristics: [
      new TempOneCharacteristic(),
      new TempTwoCharacteristic(),
      new SwitchCharacteristic()
      // new LedCharacteristic()
    ]
  });
};

util.inherits(SystemInformationService, bleno.PrimaryService);
module.exports = SystemInformationService;
