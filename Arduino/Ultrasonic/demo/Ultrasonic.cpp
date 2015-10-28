#include "Arduino.h"
#include "Ultrasonic.h"

Ultrasonic::Ultrasonic(int TP, int EP){
   pinMode(TP,OUTPUT);
   pinMode(EP,INPUT);
   Trig_pin=TP;
   Echo_pin=EP;
}

long Ultrasonic::MeasureTiming(){
  digitalWrite(Trig_pin, LOW); // Ensure low
  delayMicroseconds(2); // Wait for stable low
  
  digitalWrite(Trig_pin, HIGH); // Set trigger pin to high for 10 microseconds
  delayMicroseconds(10);        // to initiate sensor cycle
  
  digitalWrite(Trig_pin, LOW); // Then set low again, the sensor will now emit and receive
                               // 8 cycles of supersonic sound waves at 40 khz
                               
  return pulseIn(Echo_pin,HIGH);  // Now wait for the sensor to output a pulse,
                                  // the pulse width corresponds to a distance,
                                  // given delta t from send to recieved.
}

long Ultrasonic::GetRange(){
  pulseWidth = MeasureTiming();
  return pulseWidth/58; // Optimisesesesesesesationsssssss
  //return (speedOfSound*pulseWidth)/2;   //L = C × T/2 
                                          //where L is the length,
                                          //C is the speed of sound in air(cm/us),
                                          //T is the time difference from the transmission(us) 
                                          //from the transmitter to the receiver.
                                          //This is divided by 2 for the two-directions the sound travels.
}
