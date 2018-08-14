var bleno = require('bleno');

const constants = require('./constants')

var SystemInformationService = require('./systeminformationservice');

var systemInformationService = new SystemInformationService();
var rpio = require('rpio');

bleno.on('stateChange', function(state) {
  console.log('on -> stateChange: ' + state);

  if (state === 'poweredOn') {
    bleno.startAdvertising(constants.THERMAL_DIVIDER_DEVICE_NAME, [systemInformationService.uuid]);
  } else {
    bleno.stopAdvertising();
  }
});

bleno.on('advertisingStart', function(error) {

  console.log('on -> advertisingStart: ' +
    (error ? 'error ' + error : 'success')
  );

  if (!error) {

    rpio.write(constants.RELAY_PIN, rpio.LOW);
    rpio.write(constants.LED_PIN, rpio.LOW);

    bleno.setServices([
      systemInformationService
    ]);
  }
});
