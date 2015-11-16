#include "SensorPacketBuilder.h"

SensorPacketBuilder::SensorPacketBuilder(){
  memset(buffer, 0, 64);
  remainingBitsInByte = 8;
  currentByteIndex = 0;
}

void SensorPacketBuilder::add(unsigned long value, unsigned int bitsToSet){

  if(bitsToSet > remainingBitsInByte) { //value fits into current byte, so it can just be set
    byte valueByte = value & 0xff;
    byte bitsToSetByte = bitsToSet & 0xff;
    setBitsInByte(valueByte, bitsToSetByte);
  } else {
    byte bitsFitIntoCurrByte = bitsToSet - remainingBitsInByte;
    setBitsInByte(getNBits(value, bitsFitIntoCurrByte), bitsFitIntoCurrByte); //sets number of bits left in byte
    currentByteIndex++; //byte full, increment to next
    remainingBitsInByte = 8;
    add(value>>bitsFitIntoCurrByte, bitsToSet-bitsFitIntoCurrByte); //remove already set bytes from value, and recursively call function
  }
}
void SensorPacketBuilder::setBitsInByte(byte value, byte bitsToSet) {
  byte currentByte = buffer[currentByteIndex];
  currentByte = currentByte << bitsToSet;
  currentByte = currentByte | value;
  buffer[currentByteIndex] = currentByte;
  remainingBitsInByte -= bitsToSet;
}

byte SensorPacketBuilder::getNBits(byte value, byte bitsToGet) {
  value = value << bitsToGet;
  return value >> bitsToGet;
}

// Writes the packet data into the buffer given as parameter
// returns the number of bytes written to the outputBuffer
unsigned int SensorPacketBuilder::build(byte* outputBuffer) {
  unsigned int size = sizeof(sensorPacket);
  memcpy(outputBuffer, &sensorPacket, size);
  return size;
}
