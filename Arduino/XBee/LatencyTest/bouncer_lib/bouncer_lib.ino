#include <XBee.h>

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0xFFFF); // Broadcasting (white, blue)
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
  pinMode(12, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(10, OUTPUT);
  pinMode(9, OUTPUT);
  digitalWrite(12, LOW);
  digitalWrite(10, LOW);
  
  Serial.begin(9600);
  xbee.begin(Serial);
  
  int i;
  for(i = 0; i < 2; i++){
    digitalWrite(13, HIGH);
    digitalWrite(11, HIGH);
    digitalWrite(9, HIGH);
    delay(500);
    digitalWrite(13, LOW);
    digitalWrite(11, LOW);
    digitalWrite(9, LOW);
    delay(500);
  }
}

void loop(){
  xbee.readPacket();
    
    if (xbee.getResponse().isAvailable()) {
      // got something
      
      if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
        // got a zb rx packet
        
        // now fill our zb rx class
        xbee.getResponse().getZBRxResponse(rx);
            
        if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
            // the sender got an ACK
            Serial.println("P2P packet acknowledged");
        } else if (rx.getOption() == ZB_BROADCAST_PACKET) {
            Serial.println("Broadcast packet acknowledged");
        }
        Serial.print("Package: ");
        Serial.println((char *)rx.getData());
      } else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
        xbee.getResponse().getModemStatusResponse(msr);
        // the local XBee sends this response on certain events, like association/dissociation
        
        if (msr.getStatus() == ASSOCIATED) {
          // yay this is great.  flash led
          Serial.print("MSR ASSOCIATED");
          Serial.println(msr.getStatus());
          flashLed(statusLed, 10, 10);
        } else if (msr.getStatus() == DISASSOCIATED) {
          // this is awful.. flash led to show our discontent
          Serial.print("MSR DISASSOCIATED");
          Serial.println(msr.getStatus());
          flashLed(errorLed, 10, 10);
        } else {
          // another status
          Serial.print("MSR ???");
          Serial.println(msr.getStatus());
          flashLed(statusLed, 5, 10);
        }
      } else {
        // not something we were expecting
        flashLed(errorLed, 1, 25);
        Serial.print("Unexpected ApiId: ");
        Serial.println(xbee.getResponse().getApiId());
      }
      Serial.println("___________");
    } else if (xbee.getResponse().isError()) {
      Serial.print("Error reading packet.  Error code: ");  
      Serial.println(xbee.getResponse().getErrorCode());
    }
  /*xbee.readPacketUntilAvailable();
  Serial.print("apiId: ");
  Serial.println(xbee.getResponse().getApiId());
  xbee.getResponse().reset();
  xbee.flush();
  Serial.flush();
  /*
  if(xbee.getResponse().isAvailable()){
    int apiId = xbee.getResponse().getApiId();
    digitalWrite(12, HIGH);
    delay(250);
    digitalWrite(12, LOW);
    //delay(250);
    Serial.print("SUCCESS apiId: ");
    Serial.println(apiId);
      /*if (apiId == ZB_RX_RESPONSE) {
        xbee.getResponse().getZBRxResponse(rx);
        int rxOption = rx.getOption();
        if (rxOption == ZB_PACKET_ACKNOWLEDGED) {
          xbee.send(zbTx);
        } else {
          digitalWrite(13, HIGH);
          delay(500);
          digitalWrite(13, LOW);
        }
      } else {
        digitalWrite(12, HIGH);
        delay(500);
        digitalWrite(12, LOW);
      }
    }
  } else {
    int apiId = xbee.getResponse().getApiId();
    Serial.print("ERROR apiId: ");
    Serial.println(apiId); 
    if(xbee.getResponse().isError()){
      Serial.print("Error: ");
      Serial.println(xbee.getResponse().getErrorCode());
    }
    digitalWrite(13, HIGH);
    delay(250);
    digitalWrite(13, LOW);
    //delay(250);
  }*/
}

void serialEvent(){}
