#include "SensorPacketBuilder.h"

SensorPacketBuilder::SensorPacketBuilder(){
  memset(buffer, 0, 64);
  remainingBitsInByte = 8;
  currentByteIndex = 0;
}

void SensorPacketBuilder::add(unsigned long value, unsigned int bitsToSet){
  byte valueByte = value & 0xff;
  byte bitsToSetByte = bitsToSet & 0xff;
  if(bitsToSet <= remainingBitsInByte) { //value fits into current byte, so it can just be set
    setBitsInByte(valueByte, bitsToSetByte);
  } else {


    // the following two calculations MUST happen before setBitsInByte call, as setBitsInByte can mutate remainingBitsInByte
    /////// ************************************************************* ///////
    byte bitsToSetRecursively = bitsToSet - remainingBitsInByte;
    // remove already set bytes from value
    unsigned long valueToUseRecursively = value >> remainingBitsInByte;
    /////// ************************************************************* ///////
    setBitsInByte(valueByte, remainingBitsInByte); //sets number of bits left in byte
    add(valueToUseRecursively, bitsToSetRecursively); // recursively call function
  }
}

void SensorPacketBuilder::setBitsInByte(byte value, byte bitsToSet) {
  byte currentByte = buffer[currentByteIndex];
  // We do not want to shift the existing bits, so we shift the bits of value to the left, so existing bits are skipped. This works even if if no existing bits are set, as 8 - 8 = 0.
  value = value << (8 - remainingBitsInByte);
  currentByte = currentByte | value;
  buffer[currentByteIndex] = currentByte;
  remainingBitsInByte -= bitsToSet;
  if(remainingBitsInByte == 0) {
    currentByteIndex++;
    remainingBitsInByte = 8;
  }
}

// Writes the packet data into the buffer given as parameter
// returns the number of bytes written to the outputBuffer
unsigned int SensorPacketBuilder::build(byte* outputBuffer) {
  byte size = currentByteIndex + 1;
  memcpy(outputBuffer, buffer, size);
  // reset everything to be ready for next build
  memset(buffer, 0, 64);
  remainingBitsInByte = 8;
  currentByteIndex= 0;
  return size;
}
