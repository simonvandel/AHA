#include "Photoresistor.h"
#include "Arduino.h"

Photoresistor::Photoresistor(unsigned int pin) {
  inputPin = pin;
}

unsigned int Photoresistor::getLightIntensity() {
  return analogRead(inputPin);
}
