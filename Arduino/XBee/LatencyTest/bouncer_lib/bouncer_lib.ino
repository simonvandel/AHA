#include <Printers.h>
#include <XBee.h>

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x40700308);
ZBTxRequest zbTx;
XBee xbee = XBee();
XBeeResponse response;
char *dataRecieved;
int dataLen;

void setup() {
    Serial.begin(9600);
    pinMode(13, OUTPUT);
}

void loop(){}

void serialEvent(){
  digitalWrite(13, HIGH);
  xbee.readPacket();
  if (xbee.getResponse().isAvailable()) {
    response = xbee.getResponse();
    dataRecieved = (char *)response.getFrameData();
    dataLen = response.getFrameDataLength();
    zbTx = ZBTxRequest(addr64, (uint8_t *)dataRecieved, dataLen);
    delay(250);
    digitalWrite(13, LOW);
  } else {
    error();
  }
}

void error(){
  while(1){
    digitalWrite(13, HIGH);
    delay(250);
    digitalWrite(13, LOW);
    delay(250);
  }
}

