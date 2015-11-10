#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
XBee xbee = XBee();
char payload[64] = {0};
char *dataRecieved;
int i = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0xFFFF); // Sending to coordinator (green)
ZBTxStatusResponse txStatus = ZBTxStatusResponse();
ZBRxResponse rx = ZBRxResponse();
ZBTxRequest zbTx;

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
  for(i = 0; i < 15; i++){
    digitalWrite(13, HIGH);
    delay(500);
    digitalWrite(13, LOW);
    delay(500);
  }
  /*while(1);
  digitalWrite(13, HIGH);
  if(xbee.readPacket(1000)){
    if(xbee.getResponse().isAvailable()){
      if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
        xbee.getResponse().getZBRxResponse(rx);
        if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
          digitalWrite(13, LOW);
        }
      }
    }
  }*/
}

void loop(){
  digitalWrite(12, HIGH);
  xbee.send(zbTx);
  
  if(xbee.readPacket(500)){
    int apiId = xbee.getResponse().getApiId();
    if(apiId == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        digitalWrite(12, LOW);
        digitalWrite(13, HIGH);
        delay(250);
        digitalWrite(13, LOW);
        delay(250);
      }
    } else if (apiId == MODEM_STATUS_RESPONSE){
      digitalWrite(12, HIGH);
      digitalWrite(13, HIGH);
      delay(250);
      digitalWrite(12, LOW);
      digitalWrite(13, LOW);
      delay(250);
    }
  }

  delay(2000);
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
