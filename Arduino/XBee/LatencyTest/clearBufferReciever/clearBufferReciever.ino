#include <XBee.h>

XBee xbee = XBee();

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  xbee.setSerial(Serial);
}

void loop() {
  // put your main code here, to run repeatedly:
  //xbee.readPacketUntilAvailable();
}
