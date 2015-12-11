#include "Ultrasonic.h"
#include "PIR.h"
#include "Photoresistor.h"
#include "SensorPacketBuilder.h"

Ultrasonic ultrasonic(3,2);
PIR pir(8);
Photoresistor photoresistor(1);
SensorPacketBuilder sensorPacketBuilder;

byte buildArray[64];

void setup()
{
  Serial.begin(9600);
  memset(buildArray, 0, 64);
}

//For debugging prints char in binary
void printbincharpad(char c)
{
  int i;
  for (i = 7; i >= 0; --i)
    {
      Serial.write( (c & (1 << i)) ? '1' : '0' );
    }
  Serial.print('\n');
}

void loop()
{
  // ********** Analog readings *********
  // 32 bit analog
  unsigned long distance = ultrasonic.getDistance();
  Serial.print("Distance: ");
  Serial.println(distance);
  // 10 bit analog
  unsigned int lightIntensity = photoresistor.getLightIntensity();
  Serial.print("Light:");
  Serial.println(lightIntensity);
  Serial.println("");

  // ********** digital readings *********
  // digital sensor
  boolean motion = pir.getMotionDetected();
  Serial.print("Motion:");
  Serial.println(motion);

  // packet header
  sensorPacketBuilder.add(2, 3); // numAnalog
  sensorPacketBuilder.add(0, 3); // indexAnalog. No emulatable analog sensor
  sensorPacketBuilder.add(3, 2); // Analog size 1 = 32 bits
  sensorPacketBuilder.add(2, 2);// Analog size 2 = 10 bits
  sensorPacketBuilder.add(1, 4);// num digital
  sensorPacketBuilder.add(0, 4);// index digital. No emulatable digital sensor

  // body
  sensorPacketBuilder.add(distance, 32);// analog val 1 = distance
  sensorPacketBuilder.add(lightIntensity, 10);// analog val 2 = light
  sensorPacketBuilder.add(motion, 1);// digital val 1 = pir

  int packetSize = sensorPacketBuilder.build(buildArray);
  for(int i = 0; i < packetSize; i++) {
    printbincharpad(buildArray[i]);
  }
  memset(buildArray, 0, 64);
  Serial.println();
  delay(10000);

}