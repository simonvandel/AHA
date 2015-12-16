#include "Arduino.h"
#include "PIR.h"

PIR::PIR(unsigned int tp) {
  triggerPin = tp;
  pinMode(triggerPin, INPUT);
  readIndex = 0;
  total = 0;
  average = 0.0;
  // zero array
  for (int thisReading = 0; thisReading < NUM_READINGS; thisReading++) {
    readings[thisReading] = 0;
  }
}

// Uses code from https://www.arduino.cc/en/Tutorial/Smoothing
bool PIR::getMotionDetected() {
  // subtract the last reading:
  total = total - readings[readIndex];
  // read from the sensor:
  readings[readIndex] = digitalRead(triggerPin);
  // add the reading to the total:
  total = total + readings[readIndex];
  // advance to the next position in the array:
  readIndex = readIndex + 1;

  // if we're at the end of the array...
  if (readIndex >= NUM_READINGS) {
    // ...wrap around to the beginning:
    readIndex = 0;
  }

  // calculate the average:
  average = ((float) total) / ((float) NUM_READINGS);

  if(average <= 0.5) {
    return false;
  }
  else {
    return true;
  }
}
