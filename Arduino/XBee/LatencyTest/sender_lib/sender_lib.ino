#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
XBee xbee = XBee();
char payload[64] = {0};
char *dataRecieved;
ZBTxRequest zbTx;
int i = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0xFFFF); // Broadcasting (white, blue)
ZBTxStatusResponse txStatus = ZBTxStatusResponse();

void setup() {
  pinMode(13, OUTPUT);
  Serial.begin(9600);
  xbee.setSerial(Serial);
  
  payload[0] = 'A';
  payload[1] = 'A';
  payload[2] = 'A';
  payload[3] = 'A';
  payload[4] = 'A';
  payload[5] = 'A';
  
  zbTx = ZBTxRequest(addr64, (uint8_t *)payload, 6);

  delay(5000);
}

void loop(){
  
  digitalWrite(13, HIGH);  
  //xbee.send(zbTx);
  digitalWrite(13, LOW);
  
  xbee.readPacket(5000);
}

void serialEvent(){}
