#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
XBee xbee = XBee();
char payload[64] = {0};
char *dataRecieved;
ZBTxRequest zbTx;
int i = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x13A200, 0x407156BA); // Sending to coordinator (green)
ZBTxStatusResponse txStatus = ZBTxStatusResponse();
ZBRxResponse rx = ZBRxResponse();

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
  digitalWrite(13, HIGH);
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
  
  //xbee.send(zbTx);
  
  if(xbee.readPacket(2000)){
    int apiId = xbee.getResponse().getApiId();
    digitalWrite(13, HIGH);
    delay(500);
    digitalWrite(13, LOW);
  } else {
    digitalWrite(12, HIGH);
    delay(500);
    digitalWrite(12, LOW);
  }
  /*if(apiId == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        digitalWrite(13, LOW);
      }
    } else if (apiId == MODEM_STATUS_RESPONSE){
      Serial.println("Modem status response:");
      xbee.getResponse().getModemStatusResponse(rx);
      Serial.println((char *)rx.getFrameData());
    }*/
  // }else { Serial.println();}
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
