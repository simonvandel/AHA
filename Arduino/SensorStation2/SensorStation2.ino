#include "Ultrasonic.h"
#include "PIR.h"
#include "Photoresistor.h"
#include "SensorPacketBuilder.h"
#include "Serialization.h"
#include <XBee.h>


#define lightSwitch 13

#define btn1 6
#define btn2 5
#define btn3 4
#define btn4 2
#define event 3

#define redLeg 10
#define greenLeg 12
#define blueLeg 11

boolean lightSwitchVal = false;

enum LightColour {
  RED,
  BLUE,
  GREEN
};

LightColour currentLightColour = RED;

Serialization serialization;

long unsigned startTimeTimer = 0;
long unsigned startTimeLoop = 0;
long unsigned packages = 0;

int packetSize = 0;

SensorPacketBuilder sensorPacketBuilder;

byte buildArray[64];
//XBee radio vars and call back functions
XBeeWithCallbacks xbee;
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0x0);

void toggleLightSwitch(){
  if(lightSwitchVal){
    digitalWrite(lightSwitch, LOW);
    lightSwitchVal = false;
  }
  else{
    digitalWrite(lightSwitch, HIGH);
    lightSwitchVal = true;
  }
}

void makeColour(LightColour newLightColour){
  switch(newLightColour){
    case RED:
      digitalWrite(redLeg, LOW);
      digitalWrite(greenLeg, HIGH);
      digitalWrite(blueLeg, HIGH);
      currentLightColour = RED;
      break;
    case GREEN:
      digitalWrite(redLeg, HIGH);
      digitalWrite(greenLeg, LOW);
      digitalWrite(blueLeg, HIGH);
      currentLightColour = GREEN;
      break;
    case BLUE:
      digitalWrite(redLeg, HIGH);
      digitalWrite(greenLeg, HIGH);
      digitalWrite(blueLeg, LOW);
      currentLightColour = BLUE;
      break;
  }
}

void detColour(){
  if(!digitalRead(btn1)){
    makeColour(RED);
  }else if(!digitalRead(btn2)){
    makeColour(BLUE);
  }else if(!digitalRead(btn3)){
    makeColour(GREEN);
  }
}

void setup()
{
  pinMode(redLeg, OUTPUT);
  pinMode(btn1, INPUT);
  pinMode(greenLeg, OUTPUT);
  pinMode(btn2, INPUT);
  pinMode(blueLeg, OUTPUT);
  pinMode(btn3, INPUT);
  pinMode(event, INPUT);
  pinMode(btn4, INPUT);
  pinMode(lightSwitch, OUTPUT);

  attachInterrupt(0, toggleLightSwitch, RISING);
  attachInterrupt(1, detColour, RISING);
  Serial.begin(9600);
  xbee.setSerial(Serial);
  memset(buildArray, 0, 64);
  int i;
  for(i = 0; i < 5; i++){
    makeColour(RED);
    digitalWrite(lightSwitch, HIGH);
    delay(333);
    makeColour(GREEN);
    digitalWrite(lightSwitch, LOW);
    delay(333);
    makeColour(BLUE);
    delay(333);
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
  // Header analog
  sensorPacketBuilder.add(1, 3); // Number of analog sensors
  sensorPacketBuilder.add(0, 3); // Emulatable analog index. No emulatable analog sensors
  sensorPacketBuilder.add(2, 2); // Analog value 1 size info

  // Header digital
  sensorPacketBuilder.add(1, 3); // Number of digital sensors
  sensorPacketBuilder.add(0, 3); // Emulatable analog index

  // Body
  sensorPacketBuilder.add(lightSwitchVal, 1);
  
  switch(currentLightColour){
    case RED: sensorPacketBuilder.add(0, 2); break;
    case GREEN: sensorPacketBuilder.add(1, 2); break;
    case BLUE: sensorPacketBuilder.add(2, 2); break;
  }
}

