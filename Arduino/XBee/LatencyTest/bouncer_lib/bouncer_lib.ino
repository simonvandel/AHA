#include <Printers.h>
#include <XBee.h>

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x40700308); // Sening to blue
ZBTxRequest zbTx = ZBTxRequest();
ZBRxResponse rx = ZBRxResponse();
XBee xbee = XBee();
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
  if (xbee.getResponse().isAvailable() && 
      xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
        
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
    }
  }
  delay(250);
  digitalWrite(13, LOW);
}

void error(){
  while(1){
    digitalWrite(13, HIGH);
    delay(250);
    digitalWrite(13, LOW);
    delay(250);
  }
}

