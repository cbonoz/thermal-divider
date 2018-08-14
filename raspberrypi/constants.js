
// create switch characteristic and allow remote device to read and write
// BLECharCharacteristic switchChar("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
// BLEUnsignedCharCharacteristic temperatureService1("19B10000-E8F2-537E-4F6C-D104768A1217", BLERead | BLENotify); // create service for temp1
// BLEUnsignedCharCharacteristic temperatureService2("19B10000-E8F2-537E-4F6C-D104768A1218", BLERead | BLENotify); //create service for temp2

export const SWITCH_UUID = "19B10001-E8F2-537E-4F6C-D104768A1214"
export const TEMP1_UUID = "19B10000-E8F2-537E-4F6C-D104768A1217"
export const TEMP2_UUID = "19B10000-E8F2-537E-4F6C-D104768A1218"
export const LED_UUID = "19B10000-E8F2-537E-4F6C-D104768A1214"
