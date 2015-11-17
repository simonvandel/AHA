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
  // 32 bit analog
  unsigned long distance = ultrasonic.getDistance();
  Serial.print("Distance: ");
  Serial.println(distance);
  // digital sensor
  boolean motion = pir.getMotionDetected();
  Serial.print("Motion:");
  Serial.println(motion);
  // 10 bit analog
  unsigned int lightIntensity = photoresistor.getLightIntensity();
  Serial.print("Light:");
  Serial.println(lightIntensity);
  Serial.println("");

  sensorPacketBuilder.add(2, 3); // numAnalog
  sensorPacketBuilder.add(1, 3); // indexAnalog
  sensorPacketBuilder.add(3, 2); // Analog size 1
  sensorPacketBuilder.add(2, 2);// Analog size 2
  sensorPacketBuilder.add(3, 4);// num digital
  sensorPacketBuilder.add(2, 4);// index digital

  // body
  sensorPacketBuilder.add(4, 32);// analog val 1
  sensorPacketBuilder.add(1000, 10);// analog val 2
  sensorPacketBuilder.add(0, 1);// digital val 1
  sensorPacketBuilder.add(1, 1);// digital val 2
  sensorPacketBuilder.add(0, 1);// digital val 3

  int i;
  int size = sensorPacketBuilder.build(buildArray);
  for(i = 0; i < size; i++) {
    printbincharpad(buildArray[i]);
  }
  memset(buildArray, 0, 64);
  Serial.println();
  delay(10000);

}