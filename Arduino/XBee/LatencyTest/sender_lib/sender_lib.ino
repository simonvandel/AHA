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
ZBRxResponse rx = ZBRxResponse();

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
  
  digitalWrite(13, HIGH);
  
  xbee.send(zbTx);
  
  xbee.readPacket(500);
  if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
    xbee.getResponse().getTxStatusResponse(txStatus);
    if (txStatus.getDeliveryStatus() == SUCCESS) {
      digitalWrite(13, LOW);
    } else {
      //error();
    }
  } else {
    Serial.println();
    Serial.println(xbee.getResponse().getApiId());
    error();
  }
  digitalWrite(13, HIGH);
  delay(500);
  if(xbee.readPacket(500)){
    if(xbee.getResponse().isAvailable()){
      if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
        xbee.getResponse().getZBRxResponse(rx);
        if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
          digitalWrite(13, LOW);
        }
      }
    }
  }
}

void loop(){}

void serialEvent(){}

void error(){
  while(1){
    digitalWrite(13, HIGH);
    delay(250);
    digitalWrite(13, LOW);
    delay(250);
  }
}
