#include <XBee.h>

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0xFFFF); // Broadcasting (white, blue)
ZBTxStatusResponse txStatus = ZBTxStatusResponse();
ZBTxRequest zbTx = ZBTxRequest();
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
  for(i = 0; i < 10; i++){
    digitalWrite(13, HIGH);
    delay(500);
    digitalWrite(13, LOW);
    delay(500);
  }
}

void loop(){
  xbee.readPacket();
    
  if (xbee.getResponse().isAvailable()) {
  // got something

    if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
    // got a zb rx packet
        
      xbee.getResponse().getZBRxResponse(rx);
      // now fill our zb rx class
      
      if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
        Serial.println("Direct packet acknowledged");
      } else if (rx.getOption() == ZB_BROADCAST_PACKET) {
        digitalWrite(13, HIGH);
        Serial.println("Broadcast packet acknowledged");
        delay(2000);
        xbee.send(zbTx);
        if(xbee.readPacket(500)){
          if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
            xbee.getResponse().getZBTxStatusResponse(txStatus);
            if (txStatus.getDeliveryStatus() == SUCCESS) {
              digitalWrite(13, LOW);
            }
          }
        }
      } else {
        Serial.println("Unknown packet status");
      }
      Serial.print("Package: ");
      Serial.println((char *)rx.getData());
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
  }
}

void serialEvent(){}

