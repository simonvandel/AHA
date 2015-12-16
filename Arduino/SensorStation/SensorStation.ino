#include "Ultrasonic.h"
#include "PIR.h"
#include "Photoresistor.h"
#include "SensorPacketBuilder.h"
#include "Serialization.h"
#include <XBee.h>

#define LightSwitch1 13
#define LightSwitch2 11
#define btn1 2
#define btn2 3

boolean lightSwitch1Val = false;
boolean lightSwitch2Val = false;
Serialization serialization;

long unsigned startTime = 0;
long unsigned packages = 0;

//Ultrasonic ultrasonic(4,5);
//PIR pir(3);
//Photoresistor photoresistor(1);
SensorPacketBuilder sensorPacketBuilder;

byte buildArray[64];
//XBee radio vars and call back functions
XBeeWithCallbacks xbee;
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0x0);

//uses Printers.h so //Serial.print works differently
void zbReceive(ZBRxResponse& rx, uintptr_t) {
  Serial.println("test");
  if(rx.getDataLength() != 4) { //getDataLength hopefully returns value in bytes
    //Report error, "repeat message"-message?
    Serial.println("Received unexpected data size");
    return;
  }
  byte data[4];
  for (int i = 0; i < 4; i++) { //load data from response into byte array
    data[i] = rx.getData(i);
  }
  int mes[2];
  serialization.Deserialize(data, mes);
  if(mes[0] == 1){
    Serial.println("Happy days, got index 1");
    if(mes[1]){
      digitalWrite(LightSwitch1, HIGH);
      lightSwitch1Val = true;
    }
    else {
      digitalWrite(LightSwitch1, LOW);
      lightSwitch1Val = false;
    }
  }
  return;
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

void toggleLightSwitch2(){
  if(lightSwitch2Val){
    digitalWrite(LightSwitch2, LOW);
    lightSwitch2Val = false;
  }
  else{
    digitalWrite(LightSwitch2, HIGH);
    lightSwitch2Val = true;
  }
}

//
void setup()
{
  pinMode(LightSwitch1, OUTPUT);
  pinMode(btn1, INPUT);
  pinMode(LightSwitch2, OUTPUT);
  pinMode(btn2, INPUT);

  pinMode(12, OUTPUT);
  digitalWrite(12, LOW);
  
  attachInterrupt(0, toggleLightSwitch1, RISING);
  attachInterrupt(1, toggleLightSwitch2, RISING);
  Serial.begin(9600);
  xbee.setSerial(Serial);
  // Called when an actual packet received
  xbee.onZBRxResponse(zbReceive);
  memset(buildArray, 0, 64);
  int i;
  for(i = 0; i < 10; i++){
    digitalWrite(LightSwitch1, HIGH);
    digitalWrite(LightSwitch2, HIGH);
    delay(250);
    digitalWrite(LightSwitch1, LOW);
    digitalWrite(LightSwitch2, LOW);
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
  //Serial.print('\n');
}

void loop()
{
  //Serial.println("\n----- Loop Start -----");  
  startTime = millis();
  // ********** Analog readings *********
  // 32 bit analog
  //unsigned long distance = 1;//ultrasonic.getDistance();
  //Serial.print("LightSwitch1: ");
  //Serial.println(lightSwitch1Val);
  // 10 bit analog
  //unsigned int lightIntensity = photoresistor.getLightIntensity();

  // ********** digital readings *********
  // digital sensor
  //boolean motion = pir.getMotionDetected();
  //Serial.print("LightSwitch2: ");
  //Serial.println(lightSwitch2Val);
  // packet header
  sensorPacketBuilder.add(0, 3); // numAnalog
  sensorPacketBuilder.add(0, 3); // indexAnalog. No emulatable analog sensor
  //sensorPacketBuilder.add(0, 2); // Analog size 1 = 32 bits

  sensorPacketBuilder.add(2, 3);// num digital
  //hacks, index is index plus 1, so to address index 1 put 2, and for 2 put 3 and so on..
  sensorPacketBuilder.add(3, 3);// index digital. No emulatable digital sensor

  // body
  //sensorPacketBuilder.add(distance, 32);// analog val 1 = distance
  sensorPacketBuilder.add(lightSwitch2Val, 1);// analog val 2 = light
  sensorPacketBuilder.add(lightSwitch1Val, 1);// digital val 1 = pir

  int packetSize = sensorPacketBuilder.build(buildArray);

  sendData(buildArray, packetSize);

  // Continuously let xbee read packets and call callbacks.
  xbee.loop();
  //act on received data in the call back method zbReceive

  memset(buildArray, 0, 64);
  delay(500);

  packages++;

  //Serial.println();
  //Serial.print("----- Loop end (");
  //Serial.print(millis() - startTime);
  //Serial.print(")(");
  //Serial.print(packages);
  //Serial.println(") -----");
}

void sendData(uint8_t* toSend, int sendLen){
  for(int i = 0; i < sendLen; i++) {
    //printbincharpad(toSend[i]);
  }
  ZBTxRequest zbTx = ZBTxRequest(addr64, toSend, sendLen);
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
