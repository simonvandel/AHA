#include <XBeeLibrary.h>
#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
XBee xbee = XBee();
char payload[72] = {0};
int results[64] = {0};
int i = 0;
int payloadSize = 0;
// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0, 0); // Sending to coordinator (green)
ZBRxResponse rx = ZBRxResponse();

void setup() {
  pinMode(13, OUTPUT);
  
  Serial.begin(9600);
  xbee.setSerial(Serial);

  for(i = 0; i < 72; i++){
    payload[i] = 'A';
  }
}

void loop(){
  digitalWrite(13, HIGH);
  Serial.flush();
  payloadSize++;
  if(payloadSize > 72)
    payloadSize = 1;
    
  long startTime = millis();
  if(sendData(addr64, (byte*) payload, payloadSize, xbee) == 0){
    Serial.print(payloadSize);
    Serial.print(" data send successfully, in: ");
    Serial.println((millis() - startTime));
   } else {
    Serial.println("Data not send");
  }
  delay(500);
}

int recieveMessage(){
  if(xbee.readPacket(500)){
    if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
      xbee.getResponse().getZBRxResponse(rx);
      if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
        return 1;       // Direct packet recieved
      } else if (rx.getOption() == ZB_BROADCAST_PACKET) {
        return 1;       // Broadcast packet recieved
      } else {
        return 0;       // Unknown packet recieved
      }
    } else {
       return 0; // Recieved package of wrong type
    }
  } else {
    if (xbee.getResponse().isError()) {
      return 0; // Error reading packet
    }
  }
  return 0; // No package recieved
}
