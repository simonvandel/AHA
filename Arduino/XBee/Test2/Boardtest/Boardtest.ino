void setup(){
  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);
  Serial.begin(9600);
}

void loop(){
  String sA0 = String(analogRead(A0), DEC);
  Serial.print("A0: " + sA0);
  String sA1 = String(analogRead(A1), DEC);
  Serial.print(" | A1: " + sA1);
  String sA2 = String(analogRead(A2), DEC);
  Serial.print(" | A2: " + sA2);
  Serial.print('\n');
}
