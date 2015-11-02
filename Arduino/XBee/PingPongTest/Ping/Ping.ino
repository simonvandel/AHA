int const ledpin = 13;
char pong[2];

void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  pinMode(ledpin, OUTPUT);
}

void loop(){
  digitalWrite(ledpin, HIGH);            // Has ping
  delay(5000);
  Serial.write('1');                     // Send ping
  digitalWrite(ledpin, LOW);             // Loose ping
  int timeout = 1000;
  while(timeout && !Serial.available()){ // Wait for pong
    timeout--;
    delay(10);
  }
  Serial.readBytes(pong, 2);             // Save pong and terminal character
  if(pong[0] == '1' && pong[1] == 0x0D){
    recognised();
  } else {
    notRecognised();
  }
}

void recognised(){
  int i;
  for(i = 0; i < 1; i++){
    digitalWrite(ledpin, HIGH);
    delay(100);
    digitalWrite(ledpin, LOW);
    delay(100);
  }
}

void notRecognised(){
  int i;
  for(i = 0; i < 3; i++){
    digitalWrite(ledpin, HIGH);
    delay(100);
    digitalWrite(ledpin, LOW);
    delay(100);
  }
}
