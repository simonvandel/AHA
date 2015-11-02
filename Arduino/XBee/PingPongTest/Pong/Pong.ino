int const ledpin = 13;
char ping[2];

void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  pinMode(ledpin, OUTPUT);
  digitalWrite(ledpin, HIGH);
  delay(250);
  digitalWrite(ledpin, LOW);
}

void loop(){}

void serialEvent(){
  Serial.readBytes(ping, 2);  // Recieve ping
  digitalWrite(ledpin, HIGH); // Has ping
  delay(500);
  Serial.write((byte *)ping, 2);      // Send pong
  digitalWrite(ledpin, LOW);  // Loose ping
}
