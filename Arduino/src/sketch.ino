#include "Ultrasonic.h"
#include "PIR.h"
#include "Photoresistor.h"

Ultrasonic ultrasonic(3,2);
PIR pir(8);
Photoresistor photoresistor(1);

void setup()
{
  Serial.begin(9600);
}

void loop()
{
  unsigned long distance = ultrasonic.getDistance();
  Serial.print("Distance: ");
  Serial.println(distance);
  boolean motion = pir.getMotionDetected();
  Serial.print("Motion:");
  Serial.println(motion);
  unsigned int lightIntensity = photoresistor.getLightIntensity();
  Serial.print("Light:");
  Serial.println(lightIntensity);
  Serial.println("");
}