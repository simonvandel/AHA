int const ledpin = 13;

void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  pinMode(ledpin, OUTPUT);
}

void loop(){
  digitalWrite(ledpin, HIGH);
  Serial.write('1');
  Serial.print('\n');
  delay(100);
  digitalWrite(ledpin, LOW);
  delay(1900);
  digitalWrite(ledpin, HIGH);
  Serial.write('0');
  Serial.print('\n');
  delay(100);
  digitalWrite(ledpin, LOW);
  delay(1900);
}
