void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
}

void loop(){
  int i;
  for(i = 10; i > 0; i--){
    Serial.print(i);
    Serial.print('\n');
    delay(1000);
  }
  char start[4] = "+++";
  start[3] = 0x0D;
  Serial.write(start);
  Serial.print('\n');
  while(!Serial.available()){
    Serial.print('.');
    delay(1000);
  }
  Serial.print(Serial.read());
  Serial.print('\n');
}
