#include "Ultrasonic.h"
#include "PIR.h"
#include "Photoresistor.h"
#include "SensorPacketBuilder.h"
#include "Serialization.h"
#include <XBee.h>

#define LightSwitch1 13
#define LightSwitch2 11
#define LightSwitch3 9
#define startTimingSwitch 8
#define btn1 2
#define btn2 5
#define btn3 4
#define event2 3

boolean lightSwitch1Val = false;
boolean lightSwitch2Val = false;
boolean lightSwitch3Val = false;
boolean startTimingSwitchVal = false;
boolean messageReady = false;
Serialization serialization;

int packetSize = 0;

long unsigned startTimeTimer = 0;
long unsigned startTimeLoop = 0;
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
  if(startTimingSwitchVal) {
    Serial.print("It took ");
    Serial.print(millis() - startTimeTimer);
    Serial.println("ms to receive an action");
    startTimingSwitchVal = false;
  }
  messageReady = true;
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
  encode();
}

void toggleLightSwitch23(){   
  //startTimeTimer = millis();
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
  encode();
}

//
void setup()
{
  pinMode(LightSwitch1, OUTPUT);
  pinMode(btn1, INPUT);
  pinMode(LightSwitch2, OUTPUT);
  pinMode(btn2, INPUT);
  pinMode(LightSwitch3, OUTPUT);
  pinMode(btn3, INPUT);
  pinMode(event2, INPUT);

  pinMode(startTimingSwitch, INPUT);

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
  //Serial.print('\n');
}

void loop()
{
 // if(!digitalRead(startTimingSwitch) && !startTimingSwitchVal) {
 //   Serial.println("started timing");
 //   startTimingSwitchVal = true;
 //}
  if(!digitalRead(startTimingSwitch) && !startTimingSwitchVal) {
    Serial.println("started timing");
    if(lightSwitch2Val == 0) {
       digitalWrite(LightSwitch2, HIGH); 
    } else {
      digitalWrite(LightSwitch2, LOW); 
    }
    lightSwitch2Val = !lightSwitch2Val;
    startTimingSwitchVal = true;    
    startTimeTimer = millis();
    toggleLightSwitch23();
  }

  encode();

  sendData(buildArray, packetSize);
  packetSize = 0;

  startTimeLoop = millis();
  // Continuously let xbee read packets and call callbacks.
  
  while((millis() - startTimeLoop) < 100 && !messageReady){
    xbee.loop();
  }
  if(millis() - startTimeLoop < 100){
    Serial.print("Loop Time: ");
    Serial.println(millis() - startTimeLoop);
  }
  messageReady = false;
  //act on received data in the call back method zbReceive


  packages++;

  //Serial.println();
  //Serial.print("----- Loop end (");
  //Serial.print(millis() - startTime);
  //Serial.print(")(");
  //Serial.print(packages);
  //Serial.println(") -----");
}

void encode() {
  sensorPacketBuilder.add(0, 3); // Number of analog sensors
  sensorPacketBuilder.add(0, 3); // Emulatable analog index. No emulatable analog sensors

  sensorPacketBuilder.add(3, 3); // Number of digital sensors
  sensorPacketBuilder.add(3, 3); // Emulatable analog index

  sensorPacketBuilder.add(lightSwitch3Val, 1); // Digital sensor 3
  sensorPacketBuilder.add(lightSwitch2Val, 1); // Digital sensor 2
  sensorPacketBuilder.add(lightSwitch1Val, 1); // Digital sensor 1. Emulatable

  packetSize = sensorPacketBuilder.build(buildArray);
}

void sendData(uint8_t* toSend, int sendLen){
  /*for(int i = 0; i < sendLen; i++) {
    //printbincharpad(toSend[i]);
  }*/
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

/*int lock_var = 0; // actual lock global variable used to provide synchronization
int value; // variable contain lock value merely to explain current state

int acquire_lock(int lock){
  while
    __asm  // Inline assembly is written this way in c
    {
        mov eax, 1   // EAX is a 32 bit register in which we are assigning 1
        xchg eax, lock_var // Exchange eax and lock_var atomically
        mov value, eax // merely saving for printing purpose
    }
}

void free_lock(int lock){
  asm volatile(
    "mov eax, 0;"
    xchg eax, lock_var  // This could have been a single assignment lock_var = 0
    mov value, eax // But we are merely using xchg again to see the previous value
  )
}*/