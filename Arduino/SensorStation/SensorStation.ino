#include "Ultrasonic.h"
#include "PIR.h"
#include "Photoresistor.h"
#include "SensorPacketBuilder.h"
#include "Serialization.h"
#include <XBee.h>

#define LightSwitch1 13
#define LightSwitch2 11
#define LightSwitch3 9
#define btn1 2
#define btn2 5
#define btn3 4
#define event2 3

boolean lightSwitch1Val = false;
boolean lightSwitch2Val = false;
boolean lightSwitch3Val = false;
Serialization serialization;

long unsigned startTimeTimer = 0;
long unsigned startTimeLoop = 0;
long unsigned packages = 0;

int packetSize = 0;

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

void toggleLightSwitch23(){   
  startTimeTimer = millis();
  if(!digitalRead(btn2)){
    if(lightSwitch2Val){
      digitalWrite(LightSwitch2, LOW);
      lightSwitch2Val = false;
    }
    else{
      digitalWrite(LightSwitch2, HIGH);
      lightSwitch2Val = true;
    }
  }
  if(!digitalRead(btn3)){
    if(lightSwitch3Val){
      digitalWrite(LightSwitch3, LOW);
      lightSwitch3Val = false;
    }
    else{
      digitalWrite(LightSwitch3, HIGH);
      lightSwitch3Val = true;
    }
  }
}

void setup()
{
  pinMode(LightSwitch1, OUTPUT);
  pinMode(btn1, INPUT);
  pinMode(LightSwitch2, OUTPUT);
  pinMode(btn2, INPUT);
  pinMode(LightSwitch3, OUTPUT);
  pinMode(btn3, INPUT);
  pinMode(event2, INPUT);

  pinMode(12, OUTPUT);
  pinMode(10, OUTPUT);
  digitalWrite(12, LOW);
  digitalWrite(10, LOW);
  
  attachInterrupt(0, toggleLightSwitch1, RISING);
  attachInterrupt(1, toggleLightSwitch23, RISING);
  Serial.begin(9600);
  xbee.setSerial(Serial);
  // Called when an actual packet received
  xbee.onZBRxResponse(zbReceive);
  memset(buildArray, 0, 64);
  int i;
  for(i = 0; i < 10; i++){
    digitalWrite(LightSwitch1, HIGH);
    digitalWrite(LightSwitch2, HIGH);
    digitalWrite(LightSwitch3, HIGH);
    delay(250);
    digitalWrite(LightSwitch1, LOW);
    digitalWrite(LightSwitch2, LOW);
    digitalWrite(LightSwitch3, LOW);
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
  encode();

  packetSize = sensorPacketBuilder.build(buildArray);

  sendData(buildArray, packetSize);
  //sendData((uint8_t *)"aaaaaaaa", 4);

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
  sensorPacketBuilder.add(0, 3); // Number of analog sensors
  sensorPacketBuilder.add(0, 3); // Emulatable analog index. No emulatable analog sensors

  sensorPacketBuilder.add(3, 3); // Number of digital sensors
  sensorPacketBuilder.add(3, 3); // Emulatable analog index

  sensorPacketBuilder.add(lightSwitch3Val, 1); // Digital sensor 3
  sensorPacketBuilder.add(lightSwitch2Val, 1); // Digital sensor 2
  sensorPacketBuilder.add(lightSwitch1Val, 1); // Digital sensor 1. Emulatable
}

