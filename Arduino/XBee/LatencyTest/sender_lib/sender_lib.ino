#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
XBee xbee = XBee();
char payload[64] = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
char *dataRecieved;
int i = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0xFFFF); // Sending to coordinator (green)
ZBTxStatusResponse txStatus = ZBTxStatusResponse();
ZBRxResponse rx = ZBRxResponse();
ZBTxRequest zbTx;

void setup() {
  payload[63] = 'A';
  pinMode(13, OUTPUT);
  
  Serial.begin(9600);
  xbee.setSerial(Serial);
    
  zbTx = ZBTxRequest(addr64, (uint8_t *)payload, 1);
  int i;
  for(i = 0; i < 10; i++){
    digitalWrite(13, HIGH);
    delay(500);
    digitalWrite(13, LOW);
    delay(500);
  }
  Serial.flush();
  timeStart = millis();
  xbee.send(zbTx);
}

void loop(){ 
  digitalWrite(13, HIGH);  
  if(xbee.readPacket(10000)){
    if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        //Serial.println("Packet delivered successfully");
        digitalWrite(13, LOW);
        if(xbee.readPacket(10000)){
          if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
            xbee.getResponse().getZBRxResponse(rx);
            if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
              timeEnd = millis();
              digitalWrite(13, HIGH);
              Serial.println("Direct packet acknowledged. Time: ");
              Serial.println(timeEnd - timeStart);
              delay(1000);
              timeStart = millis();
              xbee.send(zbTx);
            } else if (rx.getOption() == ZB_BROADCAST_PACKET) {
              timeEnd = millis();
              digitalWrite(13, HIGH);
              Serial.print("Broadcast packet acknowledged. Time: ");
              Serial.println(timeEnd - timeStart);
              delay(250);
              timeStart = millis();
              xbee.send(zbTx);
            } else {
              Serial.println("Unknown packet status");
            }
            Serial.print("Package: ");
            Serial.println((char *)rx.getData());
          } else {
            Serial.print("Recieved package of wrong type, type: ");
            Serial.println(xbee.getResponse().getApiId());
          }
        } else {
          if (xbee.getResponse().isError()) {
            Serial.print("Error reading packet.  Error code: ");  
            Serial.println(xbee.getResponse().getErrorCode());
          } else {
            Serial.println("No package recieved.");
          }
          Serial.println("Restarting.");
          setup();
        }
      }
    } else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE){}
  } else {
    Serial.println("Status response missed. Restarting.");
    setup();
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
