#include "Arduino.h"
#include "PIR.h"

PIR::PIR(unsigned int tp) {
  triggerPin = tp;
  pinMode(triggerPin, INPUT);
}

// Uses code from https://www.arduino.cc/en/Tutorial/Smoothing
bool PIR::getMotionDetected() {
  unsigned int result = 0;
  int i;
  for (i = 0; i < NUM_READINGS; i++) {
    result += digitalRead(triggerPin);
  }
  float average = ((float) result) / ( (float)NUM_READINGS );
  if(average <= 0.5) {
    return false;
  }
  else {
    return true;
  }
}
