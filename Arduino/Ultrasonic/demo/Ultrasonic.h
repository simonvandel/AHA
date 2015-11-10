#ifndef Ultrasonic_h
  #define Ultrasonic_h

  #define speedOfSound 0.03432 // ~343.2 m/s at 20 degrees celcius room temperature
                               // 0.03432 cm/us
  
  #include "Arduino.h"
  
  class Ultrasonic{
    public:
      Ultrasonic(int TP, int EP);
      unsigned long MeasureTiming();
      unsigned long GetDistance();
  
      private:
      int Trig_pin;
      int Echo_pin;
      unsigned long pulseWidth;
  };
#endif
