#include "Photoresistor.h"
#include "SensorPacketBuilder.h"
#include "Serialization.h"
#include <XBee.h>

#define LightSwitch1 13
#define lightBtn 2
#define timerBtn 8
#define photoresistorPin A5

boolean lightSwitch1Val = false;
unsigned int photoresistorVal = 0;
Serialization serialization;

long unsigned startTimeTimer = 0;
long unsigned startTimeLoop = 0;
long unsigned packages = 0;

int packetSize = 0;


Photoresistor photoresistor(photoresistorPin);
SensorPacketBuilder sensorPacketBuilder;

byte buildArray[64];
//XBee radio vars and call back functions
XBeeWithCallbacks xbee;
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0x0);

//uses Printers.h so //Serial.print works differently
void zbReceive(ZBRxResponse& rx, uintptr_t) {
  if(rx.getDataLength() != 4) { //getDataLength hopefully returns value in bytes
    //Report error, "repeat message"-message?
    return;
  }
  byte data[4];
  for (int i = 0; i < 4; i++) { //load data from response into byte array
    data[i] = rx.getData(i);
  }
  int mes[2];
  serialization.Deserialize(data, mes);
  Serial.println();
  Serial.print("Toggle sensor ");
  Serial.print(mes[0]);
  Serial.print(" to value ");
  Serial.println(mes[1]);
  if(mes[0] == 2){
    if(mes[1]){
      digitalWrite(LightSwitch1, HIGH);
      lightSwitch1Val = true;
    }
    else {
      digitalWrite(LightSwitch1, LOW);
      lightSwitch1Val = false;
    }
  }
}

void toggleLightSwitch1(){
  if(lightSwitch1Val){
    digitalWrite(LightSwitch1, LOW);
    lightSwitch1Val = false;
  }
  else{
    digitalWrite(LightSwitch1, HIGH);
    lightSwitch1Val = true;
  }
}

void setup()
{
  pinMode(LightSwitch1, OUTPUT);

  
  attachInterrupt(digitalPinToInterrupt(lightBtn), toggleLightSwitch1, RISING);
  Serial.begin(9600);
  xbee.setSerial(Serial);
  // Called when an actual packet received
  xbee.onZBRxResponse(zbReceive);
  memset(buildArray, 0, 64);
  int i;
  for(i = 0; i < 10; i++){
    digitalWrite(LightSwitch1, HIGH);
    delay(250);
    digitalWrite(LightSwitch1, LOW);
    delay(250);
  }
}

//For debugging prints char in binary
void printbincharpad(char c)
{
  int i;
  for (i = 7; i >= 0; --i)
  {
    Serial.write( (c & (1 << i)) ? '1' : '0' );
  }
}

void loop()
{
  photoresistorVal = photoresistor.getLightIntensity();
  Serial.print("Photoresistor: ");
  Serial.println(photoresistorVal);
  
  encode();

  packetSize = sensorPacketBuilder.build(buildArray);

  sendData(buildArray, packetSize);

  packetSize = 0;
  
  startTimeLoop = millis();
  // Continuously let xbee read packets and call callbacks.
  while((millis() - startTimeLoop) < 100){
    xbee.loop();
  }
  if(millis() - startTimeLoop < 100){
    Serial.print("Loop Time: ");
    Serial.println(millis() - startTimeLoop);
  }
  //act on received data in the call back method zbReceive

  memset(buildArray, 0, 64);
}

void sendData(uint8_t* toSend, int sendLen){
  ZBTxRequest zbTx = ZBTxRequest(addr64, toSend, sendLen);
  zbTx.setAddress16(0);
  xbee.send(zbTx);
  ZBTxStatusResponse txStatus = ZBTxStatusResponse(); //not sure whether better to have as global or local
  //Re-sends, and forgets, if not succesful
  if(xbee.readPacket(250)){
    if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        return;
      }
    }
  }
}

void encode(){
  sensorPacketBuilder.add(1, 3); // Number of analog sensors
  sensorPacketBuilder.add(0, 3); // Emulatable analog index. No emulatable analog sensors

  sensorPacketBuilder.add(2, 2); // the first analog value is 10 bits

  sensorPacketBuilder.add(1, 3); // Number of digital sensors
  sensorPacketBuilder.add(1, 3); // Emulatable analog index

  sensorPacketBuilder.add(photoresistorVal, 10);
  
  sensorPacketBuilder.add(lightSwitch1Val, 1); // Digital sensor 1. Emulatable
  sensorPacketBuilder.add(0, 5); // Append stuff to make it 4 bytes.
}

