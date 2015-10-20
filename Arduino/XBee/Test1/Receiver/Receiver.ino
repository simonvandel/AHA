int myData = 0;
int const ledpin = 13;

void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  pinMode(ledpin, OUTPUT);
}

void loop(){
  if(Serial.available()){
    blink();
    digitalWrite(ledpin, HIGH);
    myData = Serial.read();
    if(myData == '1'){
      digitalWrite(ledpin, HIGH);
    }
    if(myData == '0'){
      digitalWrite(ledpin, LOW);
    }
  }
}

void blink(){
  digitalWrite(ledpin, HIGH);
  delay(1000);
  digitalWrite(ledpin, LOW);
  delay(1000);
}
