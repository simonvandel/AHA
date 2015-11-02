char dataRecieved[64] = {0};
int dataLen = 0;

void setup() {
  Serial.begin(9600);
}

void loop() {};

void serialEvent(){
  dataLen = Serial.readBytesUntil('\0', dataRecieved, 64);
  Serial.write((uint8_t *)dataRecieved, dataLen + 1);
}
