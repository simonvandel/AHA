#include "../lib/Ultrasonic.h"
Ultrasonic ultrasonic(12,13);

void setup() {
  Serial.begin(9600);      // init serial 9600 for logging
  Serial.println("ULTRASONIC-DEMO");
}

void loop(){
  Serial.print("Distance(cm) = ");
  Serial.println(ultrasonic.getDistance());
  delay(1000); // one second delay, minimum delay is 60 us delay otherwise long distance fucks up
}
