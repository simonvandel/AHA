#include <XBee.h>

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0xFFFF); // Broadcasting (white, blue)
ZBTxRequest zbTx = ZBTxRequest();
ZBRxResponse rx = ZBRxResponse();
XBee xbee = XBee();
char payload[64] = {0};
char dataRecieved[64] = {0};
int dataLen;

void setup() {
    pinMode(13, OUTPUT);
    pinMode(12, OUTPUT);
    pinMode(11, OUTPUT);
    digitalWrite(11, LOW);
    
    Serial.begin(9600);
    xbee.setSerial(Serial);
      
    payload[0] = 'A';
    payload[1] = 'A';
    payload[2] = 'A';
    payload[3] = 'A';
    payload[4] = 'A';
    payload[5] = 'A';
  
    zbTx = ZBTxRequest(addr64, (uint8_t *)payload, 6);
    int i;
    for(i = 0; i < 10; i++){
      digitalWrite(13, HIGH);
      delay(500);
      digitalWrite(13, LOW);
      delay(500);
    }
}

void loop(){
  xbee.readPacket();
    if(xbee.getResponse().isAvailable()){
      int apiId = xbee.getResponse().getApiId();
      if (apiId == ZB_RX_RESPONSE) {
        xbee.getResponse().getZBRxResponse(rx);
        int rxOption = rx.getOption();
        if (rxOption == ZB_PACKET_ACKNOWLEDGED) {
          error();
          xbee.send(zbTx);
        } else {
          Serial.println("ERROR rxOption:");
          Serial.write(rxOption);
          digitalWrite(13, HIGH);
          delay(250);
          digitalWrite(13, LOW);
        }
      } else {
        Serial.println("ERROR apiId:");
        Serial.println(apiId);
        digitalWrite(12, HIGH);
        delay(250);
        digitalWrite(12, LOW);
      }
    }
}

void serialEvent(){}

void error(){
  while(1){
    digitalWrite(13, HIGH);
    delay(250);
    digitalWrite(13, LOW);
    delay(250);
  }
}
