#include <XBee.h>
#include <XBeeLibrary.h>

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0x0); // Broadcasting (white, blue)
ZBTxStatusResponse txStatus = ZBTxStatusResponse();
ZBTxRequest zbTx;
ZBRxResponse rx = ZBRxResponse();
XBee xbee = XBee();
char dataRecieved[64] = {0};
int dataLen;
ModemStatusResponse msr = ModemStatusResponse();

int statusLed = 11;
int errorLed = 13;
int dataLed = 9;

void flashLed(int pin, int times, int wait) {
    
    for (int i = 0; i < times; i++) {
      digitalWrite(pin, HIGH);
      delay(wait);
      digitalWrite(pin, LOW);
      
      if (i + 1 < times) {
        delay(wait);
      }
    }
}

void setup() {
  pinMode(13, OUTPUT);
  
  Serial.begin(9600);
  xbee.begin(Serial);
  
  int i;
  for(i = 0; i < 1; i++){
    digitalWrite(13, HIGH);
    delay(500);
    digitalWrite(13, LOW);
    delay(500);
  }
}

void loop(){
  if(recieveMessage()){
    send(message(
  }
  /*xbee.readPacket();
  if (xbee.getResponse().isAvailable()) {
  // got something

    if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
    // got a zb rx packet
        
      xbee.getResponse().getZBRxResponse(rx);
      // now fill our zb rx class
      
      if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
        Serial.print("Direct packet acknowledged, Package: ");
        Serial.println((char *)rx.getData());
      } else if (rx.getOption() == ZB_BROADCAST_PACKET) {
        zbTx = ZBTxRequest(addr64, rx.getData(), rx.getDataLength());
        xbee.send(zbTx);
        digitalWrite(13, HIGH);
        if(xbee.readPacket(10000)){
          if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
            xbee.getResponse().getZBTxStatusResponse(txStatus);
            if (txStatus.getDeliveryStatus() == SUCCESS) {
              digitalWrite(13, LOW);
            }
          }
        } else {
          Serial.println("No status response");
          Serial.flush();
        }
      } else {
        Serial.println("Unknown packet status");
      }
    } else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
      xbee.getResponse().getModemStatusResponse(msr);
      // the local XBee sends this response on certain events, like association/dissociation

      if (msr.getStatus() == ASSOCIATED) {
        Serial.print("Connected to modem: ");
        Serial.println(msr.getStatus());
      } else if (msr.getStatus() == DISASSOCIATED) {
        Serial.print("Disconnected/No connection to modem: ");
        Serial.println(msr.getStatus());
      } else {
        Serial.print("Unknown Modem Status: ");
        Serial.println(msr.getStatus());
      }
    } else {
      Serial.print("Unexpected ApiId: ");
      Serial.println(xbee.getResponse().getApiId());
    }
    Serial.println("___________");
  } else if (xbee.getResponse().isError()) {
    Serial.print("Error reading packet.  Error code: ");  
    Serial.println(xbee.getResponse().getErrorCode());
  }*/
}

void serialEvent(){}

int sendMessage(XBeeAddress64 address, char *toSend, int sendLen){
  ZBTxRequest zbTx = ZBTxRequest(addr64, (uint8_t *)toSend, sendLen);
  xbee.send(zbTx);
  if(xbee.readPacket(500)){
    if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        return 1;
      }
    }
  }
  return 0;
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

