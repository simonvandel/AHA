#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0x0); // Sending to coordinator
ZBTxStatusResponse txStatus = ZBTxStatusResponse();
ZBTxRequest zbTx;
ZBRxResponse rx = ZBRxResponse();
XBee xbee = XBee();

char payload[64] = {0};
char dataRecieved[64] = {0};
int dataLen;
ModemStatusResponse msr = ModemStatusResponse();

int led = 13;


void setup() {
  pinMode(led, OUTPUT);

  
  Serial.begin(9600);
  xbee.begin(Serial);
  int i;
  for(i = 0; i < 1; i++){
    digitalWrite(led, HIGH);
    delay(500);
    digitalWrite(led, LOW);
    delay(500);
  }
  
  Serial.flush();
  i = 6;
  payload[i - 1] = '1';
  payload[i - 2] = '1';
  payload[i - 3] = '1';
  payload[i - 4] = '1';
  payload[i - 5] = '1';
  payload[i - 6] = '1';
  zbTx = ZBTxRequest(addr64, (uint8_t *)payload, i);
  xbee.
}

void loop() {

  /*
  delay(50000);
  Serial.print("Sending message");
  timeStart = millis();
  xbee.send(zbTx);
  xbee.readPacketUntilAvailable();
  if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
    xbee.getResponse().getZBTxStatusResponse(txStatus);
    if (txStatus.getDeliveryStatus() == SUCCESS) {
      timeEnd = millis();
      Serial.println("Got responese in: ");
      Serial.println(timeEnd - timeStart);
    }else {
      Serial.println("Delivery status not = success");  
    }
  } else {
    Serial.println("api id invalid");
  
  }
  xbee.readPacket();
  Serial.flush();
  delay(250);
*/
}

