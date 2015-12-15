#include "Photoresistor.h"
#include "Arduino.h"

Photoresistor::Photoresistor(unsigned int pin) {
  inputPin = pin;
}

unsigned int Photoresistor::getLightIntensity() {
  unsigned int result = 0;
  int i;
  for (i = 0; i < NUM_READINGS; i++) {
    result += analogRead(inputPin);
  }
  return result/NUM_READINGS;
}
