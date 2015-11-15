#include "Arduino.h"
#include "Ultrasonic.h"

Ultrasonic::Ultrasonic(int TP, int EP){
   pinMode(TP,OUTPUT);
   pinMode(EP,INPUT);
   trigPin=TP;
   echoPin=EP;
}

unsigned long Ultrasonic::measureTiming(){
  digitalWrite(trigPin, LOW); // Ensure low
  delayMicroseconds(2); // Wait for stable low

  digitalWrite(trigPin, HIGH); // Set trigger pin to high for 10 microseconds
  delayMicroseconds(10);        // to initiate sensor cycle

  digitalWrite(trigPin, LOW); // Then set low again, the sensor will now emit and receive
                               // 8 cycles of ultrasonic sound waves at 40 khz

  return pulseIn(echoPin,HIGH);  // Now wait for the sensor to output a pulse,
                                  // the pulse width corresponds to a distance,
                                  // given delta t from send to recieved.
}

unsigned long Ultrasonic::getDistance(){
  pulseWidth = measureTiming();
  return pulseWidth/58; // approximates (speedOfSound*pulseWidth)/2 
  //return (speedOfSound*pulseWidth)/2;   //L = C × T/2 
                                          //where L is the length,
                                          //C is the speed of sound in air(cm/us),
                                          //T is the time difference from the transmission(us) 
                                          //from the transmitter to the receiver.
                                          //This is divided by 2 for the two-directions the sound travels.
}
