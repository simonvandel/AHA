#include <XBee.h>

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0x0); // Sending to coordinator (green)
ZBTxRequest zbTx = ZBTxRequest();
ZBRxResponse rx = ZBRxResponse();
XBee xbee = XBee();
char payload[64] = {0};
char dataRecieved[64] = {0};
int dataLen;

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
    
    digitalWrite(13, HIGH);
    delay(250);
    digitalWrite(13, LOW);
}

void loop(){
  if(xbee.readPacket(500)){
    digitalWrite(13, HIGH);
    xbee.send(zbTx);
    digitalWrite(13, LOW);
    xbee.getResponse(rx);
    strcpy(dataRecieved, (char *)rx.getData());
    dataRecieved[6] = '\0';
    Serial.println(dataRecieved);
    /*if(xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
    
      xbee.getResponse().getZBRxResponse(rx);
      dataRecieved = (char *)xbee.getResponse().getFrameData();
      dataLen = xbee.getResponse().getFrameDataLength();
    
      if(rx.getOption() == ZB_PACKET_ACKNOWLEDGED){
        Serial.println("\nDataRecieved: ");
        Serial.write(dataRecieved, dataLen);
        zbTx = ZBTxRequest(addr64, (uint8_t *)dataRecieved, dataLen);
      } else {
        Serial.println("Package not acknowledged: ");
        Serial.write(dataRecieved, dataLen);
      }*/
    }
}

void serialEvent(){}

