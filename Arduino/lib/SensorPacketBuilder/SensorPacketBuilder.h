#include "Arduino.h"

class SensorPacketBuilder {
  public:
    SensorPacketBuilder();
    void add(unsigned long value, unsigned int bitsToSet);
    unsigned int build(byte* outputBuffer);
  private:
    byte getNBits(byte value, byte bitsToGet);
    void setBitsInByte(byte value, byte bitsToSet);
    byte buffer[64];
    byte remainingBitsInByte;
    byte currentByteIndex;
};

