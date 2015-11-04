#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
XBee xbee = XBee();
char payload[64] = {0};
char *dataRecieved;
int i = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x407155F4); // Sending to white
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
  
  ZBTxRequest zbTx = ZBTxRequest(addr64, (uint8_t *)payload, 6);

  for(i = 0; i < 10; i++){
    digitalWrite(13, HIGH);
    delay(500);
    digitalWrite(13, LOW);
    delay(500);
  }
  digitalWrite(13, HIGH);
  timeStart = millis();
  xbee.send(zbTx);

  if(xbee.readPacket(500)){
    if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        digitalWrite(13, LOW);
      }
    }
  }
}

void loop(){}

void serialEvent(){
  timeEnd = millis();
  dataRecieved = (char *)xbee.getResponse().getFrameData();
  Serial.print("YAY!");
  digitalWrite(13, HIGH);
  //Serial.print(timeEnd - timeStart);
  //Serial.print("dataRecieved: ");
  //Serial.print((int)dataRecieved[0]);
  //Serial.print(' ');
  //Serial.print((int)dataRecieved[1]);
}
