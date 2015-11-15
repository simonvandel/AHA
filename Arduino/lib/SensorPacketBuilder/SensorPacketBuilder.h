#include "Arduino.h"
#include <limits.h>

#define BITMASK(b) (1 << ((b) % CHAR_BIT))
#define BITSLOT(b) ((b) / CHAR_BIT)
#define BITSET(a, b) ((a)[BITSLOT(b)] |= BITMASK(b))
#define BITCLEAR(a, b) ((a)[BITSLOT(b)] &= ~BITMASK(b))
#define BITTEST(a, b) ((a)[BITSLOT(b)] & BITMASK(b))
#define BITNSLOTS(nb) ((nb + CHAR_BIT - 1) / CHAR_BIT)

struct SensorPacket {
  byte numAnalogValues : 3; // using bit fields: 3 bits
  byte indexAnalogEmulatable : 3;
  // byte analogValue1 : 2;
  // byte analogValue2 : 2;
  // byte analogValue3 : 2;
  // byte analogValue4 : 2;
  // byte analogValue5 : 2;
  // byte analogValue6 : 2;
  byte numDigitalValues : 4;
  byte indexDigitalEmulatable : 4;
};

class SensorPacketBuilder {
  public:
    SensorPacketBuilder();
    void add(boolean data, boolean isEmulatable);
    unsigned int build(byte* outputBuffer);
  private:
    SensorPacket sensorPacket;
};

