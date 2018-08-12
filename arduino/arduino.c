
#include <CurieBLE.h>
int a;
int c;
float temperature1;          //temperature sensor on A0 
float temperature2;          //temperature sensor on A1 
int B=3975;                  //B value of the thermistor
float resistance;
float resistance1;

const int ledPin = 4; // set ledPin to use on-board LED
BLEPeripheral blePeripheral; // create peripheral instance

BLEService ledService("19B10000-E8F2-537E-4F6C-D104768A1214"); // create service

// create switch characteristic and allow remote device to read and write
BLECharCharacteristic switchChar("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEUnsignedCharCharacteristic temperatureService1("19B10000-E8F2-537E-4F6C-D104768A1217", BLERead | BLENotify); // create service for temp1
BLEUnsignedCharCharacteristic temperatureService2("19B10000-E8F2-537E-4F6C-D104768A1218", BLERead | BLENotify); //create service for temp2

void setup() {
  Serial.begin(9600);
  pinMode(ledPin, OUTPUT); // use the LED on pin 13 as an output

  // set the local name peripheral advertises
  blePeripheral.setLocalName("LUNCHBOX");
  // set the UUID for the service this peripheral advertises
  blePeripheral.setAdvertisedServiceUuid(ledService.uuid());

  // add service and characteristic
  blePeripheral.addAttribute(ledService);
  blePeripheral.addAttribute(switchChar);
  blePeripheral.addAttribute(temperatureService1);
  blePeripheral.addAttribute(temperatureService2);

  // assign event handlers for connected, disconnected to peripheral
  blePeripheral.setEventHandler(BLEConnected, blePeripheralConnectHandler);
  blePeripheral.setEventHandler(BLEDisconnected, blePeripheralDisconnectHandler);

  // assign event handlers for characteristic
  switchChar.setEventHandler(BLEWritten, switchCharacteristicWritten);
  // set an initial value for the characteristic
  switchChar.setValue(0);

  // advertise the service
  blePeripheral.begin();
  Serial.println(("Bluetooth device active, waiting for connections..."));
}

void loop() {
  // poll peripheral
  a=analogRead(0);
  c=analogRead(1);
  resistance=(float)(1023-a)*10000/a; //get the resistance of the sensor1;
  resistance1=(float)(1023-c)*10000/c; //get the resistance of the sensor2;
  temperature1=1/(log(resistance/10000)/B+1/298.15)-273.15;//convert to temperature via datasheet&nbsp;;
  temperature2=1/(log(resistance1/10000)/B+1/298.15)-273.15;//convert to temperature via datasheet
  temperatureService1.setValue(temperature1);
  temperatureService2.setValue(temperature2);
  delay(1000);
  Serial.print("Current temperature is ");
  Serial.println(temperature1);
  Serial.println(temperature2);
  digitalWrite(ledPin, HIGH);;
}

void blePeripheralConnectHandler(BLECentral& central) {
  // central connected event handler
  Serial.print("Connected event, central: ");
  Serial.println(central.address());
}

void blePeripheralDisconnectHandler(BLECentral& central) {
  // central disconnected event handler
  Serial.print("Disconnected event, central: ");
  Serial.println(central.address());
}

void switchCharacteristicWritten(BLECentral& central, BLECharacteristic& characteristic) {
  // central wrote new value to characteristic, update LED
  Serial.print("Characteristic event, written: ");

  if (switchChar.value()) {
    Serial.println("LED on");
    digitalWrite(ledPin, HIGH);
  } else {
    Serial.println("LED off");
    digitalWrite(ledPin, LOW);
  }
}
