int const buttonin = 2;
int const ledpin = 13;

void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  pinMode(buttonin, INPUT);
  pinMode(ledpin, OUTPUT);
}

void loop(){
  if(digitalRead(buttonin)){
    Serial.write('1');
    digitalWrite(ledpin, HIGH);
  }else{
    Serial.write('0');
    digitalWrite(ledpin, LOW);
  }
}
