int myData = 0;
int const ledpin = 13;

void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  pinMode(ledpin, OUTPUT);
}

void loop(){
  if(Serial.available()){
    myData = Serial.read();
    digitalWrite(ledpin, HIGH);
    delay(500);
    digitalWrite(ledpin, LOW);
    if(myData == '1'){
      digitalWrite(ledpin, HIGH);
    } else {
      digitalWrite(ledpin, LOW);
    }
  }
}
