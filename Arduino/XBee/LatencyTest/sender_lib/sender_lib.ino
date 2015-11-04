#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
XBee xbee = XBee();
char payload[64] = {0};
char *dataRecieved;
int i = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x40700308);
ZBTxStatusResponse txStatus = ZBTxStatusResponse();

void setup() {
  pinMode(13, OUTPUT);
  Serial.begin(9600);
  xbee.setSerial(Serial);
  payload[0] = '\0';
  ZBTxRequest zbTx = ZBTxRequest(addr64, (uint8_t *)payload, i + 1);
  digitalWrite(13, HIGH);
  delay(10000);
  digitalWrite(13, LOW);
  timeStart = millis();
  xbee.send(zbTx);
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
