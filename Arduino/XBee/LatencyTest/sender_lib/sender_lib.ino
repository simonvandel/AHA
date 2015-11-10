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
  digitalWrite(13, HIGH);
  xbee.send(zbTx);
  
  if(xbee.readPacket(500)){
    if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        digitalWrite(13, LOW);
        if(xbee.readPacket(4000)){
          if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
            xbee.getResponse().getZBRxResponse(rx);
            if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
              Serial.println("Direct packet acknowledged");
            } else if (rx.getOption() == ZB_BROADCAST_PACKET) {
              digitalWrite(13, HIGH);
              Serial.println("Broadcast packet acknowledged");
            } else {
              Serial.println("Unknown packet status");
            }
            Serial.print("Package: ");
            Serial.println((char *)rx.getData());
          } else {
            Serial.println("Recieved package of wrong type");
          }
        } else {
          Serial.println("No package recieved");
        }
      }
    } else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE){}
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
