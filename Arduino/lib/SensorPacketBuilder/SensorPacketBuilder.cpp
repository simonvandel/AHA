#include "SensorPacketBuilder.h"

SensorPacketBuilder::SensorPacketBuilder(){
  sensorPacket.numAnalogValues = 0;
  sensorPacket.indexAnalogEmulatable = 0;
  // sensorPacket.analogValue1 = 0;
  // sensorPacket.analogValue2 = 0;
  // sensorPacket.analogValue3 = 0;
  // sensorPacket.analogValue4 = 0;
  // sensorPacket.analogValue5 = 0;
  // sensorPacket.analogValue6 = 0;
  sensorPacket.numDigitalValues = 0;
  sensorPacket.indexDigitalEmulatable = 0;
}

void SensorPacketBuilder::add(boolean data, boolean isEmulatable){
  sensorPacket.numDigitalValues += 1;
}

// Writes the packet data into the buffer given as parameter
// returns the number of bytes written to the outputBuffer
unsigned int SensorPacketBuilder::build(byte* outputBuffer) {
  unsigned int size = sizeof(sensorPacket);
  memcpy(outputBuffer, &sensorPacket, size);
  return size;
}
