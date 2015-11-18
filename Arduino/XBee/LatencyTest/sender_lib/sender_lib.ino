#include <XBeeLibrary.h>
#include <XBee.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
XBee xbee = XBee();
char payload[64] = {0};
int results[64] = {0};
int i = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0, 0); // Sending to coordinator (green)
ZBRxResponse rx = ZBRxResponse();

void setup() {
  pinMode(13, OUTPUT);
  
  Serial.begin(9600);
  xbee.setSerial(Serial);

  int i;
  for(i = 0; i < 10; i++){
    digitalWrite(13, HIGH);
    delay(500);
    digitalWrite(13, LOW);
    delay(500);
  }

  if(getModemStatusResponse(xbee)){
    Serial.println("Modem status response recieved");
  } else {
    Serial.println("No network");
  }
  if(getModemStatusResponse(xbee)){
    Serial.println("Modem status response recieved");
  } else {
    Serial.println("No network");
  }

  if(setATField("DH", (uint8_t *)"13A200", 6, xbee)){
    if(setATField("DL", (uint8_t *)"40700308", 8, xbee)){
      AtCommandResponse result = AtCommandResponse();
        if(getATField("DH", xbee, &result)){
        Serial.println();
        Serial.print("DH: 0x");
        for(i = 0; i < result.getValueLength(); i++){
          Serial.print("|");
          Serial.print((char)result.getValue()[i]);
        }
        Serial.println();
        if(getATField("DL", xbee, &result)){
          Serial.println();
          Serial.print("DL: 0x");
          for(i = 0; i < result.getValueLength(); i++){
            Serial.print("|");
            Serial.print((char)result.getValue()[i]);
          }
          Serial.println();
        }
      }
    }
  }
  while(1);
  
  /*i = 6;
  payload[i - 1] = '1';
  payload[i - 2] = '1';
  payload[i - 3] = '1';
  payload[i - 4] = '1';
  payload[i - 5] = '1';
  payload[i - 6] = '1';
  Serial.print("Sending message ");
  Serial.println(i);
  timeStart = millis();
  while(i <= 64){
  digitalWrite(13, HIGH);
        //Serial.println("Packet delivered successfully");
        digitalWrite(13, LOW);
        
    } else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE){}
  } else {
    Serial.println("Status response missed. Restarting.");
    //setup();
    i++;
    payload[i - 1] = 'B';
    zbTx = ZBTxRequest(addr64, (uint8_t *)payload, i);
    delay(1000);
    Serial.print("Sending message ");
    Serial.println(i);
    timeStart = millis();
    xbee.send(zbTx);
  }
  }
  for(i = 0; i < 64; i++){
    Serial.print(i);
    Serial.print(", ");
    Serial.println(results[i]);
  }*/
}

void loop(){
  delay(1000);
  digitalWrite(13, HIGH);
  Serial.flush();
  payload[0] = 'A';
  if(sendData(addr64, payload, 1, xbee)){
    Serial.println("Data send successfully");
    digitalWrite(13, LOW);
    delay(10000);
    if(recieveMessage()){
      Serial.println("Data recieved successfully");
      digitalWrite(13, HIGH);
    } else {
      Serial.println("Data not recieved");
    }
  } else {
    Serial.println("Data not send");
  }
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
