#include <XBee.h>
#include <Printers.h>
#include <XBeeLibrary.h>

unsigned long timeEnd = 0;
unsigned long timeStart = 0;

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0, 0x0); // Sending to coordinator
//ZBTxStatusResponse txStatus = ZBTxStatusResponse();
ZBTxRequest zbTx;
ZBRxResponse rx = ZBRxResponse();
XBeeWithCallbacks xbee;

char payload[64] = {0};
char dataRecieved[64] = {0};
int dataLen = 6;

int led = 13;

void zbReceive(ZBRxResponse& rx, uintptr_t) {
  Serial.print(F("Got: "));
}

void setup() {
  pinMode(led, OUTPUT);

  
  Serial.begin(9600);
  xbee.begin(Serial);
// These are called when an actual packet received
  xbee.onZBRxResponse(zbReceive);
  
  payload[dataLen - 1] = '1';
  payload[dataLen - 2] = '1';
  payload[dataLen - 3] = '1';
  payload[dataLen - 4] = '1';
  payload[dataLen - 5] = '1';
  payload[dataLen - 6] = '1';
  zbTx = ZBTxRequest(addr64, (uint8_t *)payload, dataLen);
}

void loop() {
  sendData(addr64, payload, dataLen, xbee);

  //Serial.flush();
  delay(2500);
  xbee.loop();
 // recPack();
  delay(250);
}

void recPack() {
  
  ModemStatusResponse msr = ModemStatusResponse();
  
  xbee.readPacket();
    
    if (xbee.getResponse().isAvailable()) {
      // got something
      
      if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) {
        // got a zb rx packet
        
        // now fill our zb rx class
        xbee.getResponse().getZBRxResponse(rx);
            
        if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
            // the sender got an ACK
            Serial.println("Ack");
        } else {
            // we got it (obviously) but sender didn't get an ACK
            Serial.println("AckError");
        }
        // set dataLed PWM to value of the first byte in the data
        Serial.print("Data: ");
        Serial.println((char*)rx.getData());
        Serial.print("From: MSB");
        Serial.print(rx.getRemoteAddress64().getMsb());
        Serial.print(", LSB");
        Serial.println(rx.getRemoteAddress64().getLsb());
      } else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
        xbee.getResponse().getModemStatusResponse(msr);
        // the local XBee sends this response on certain events, like association/dissociation
        
        if (msr.getStatus() == ASSOCIATED) {
          // yay this is great.  flash led
          Serial.println("MSRAss");
        } else if (msr.getStatus() == DISASSOCIATED) {
          // this is awful.. flash led to show our discontent
          Serial.println("MSRDis");
        } else {
          // another status
          Serial.println("MSRElse");
        }
      } else {
        // not something we were expecting
        Serial.println("Dunno");
      }
    } else if (xbee.getResponse().isError()) {
      //nss.print("Error reading packet.  Error code: ");  
      //nss.println(xbee.getResponse().getErrorCode());
      Serial.println("Nothing received");
    } else {
      Serial.print("errr?: ");
      Serial.println((char*)xbee.getResponse().getFrameData());
    }

}

