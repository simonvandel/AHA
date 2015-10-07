#define sensor1Pin 0
#define sensor2Pin 1
void setup() {
  Serial.begin(9600);
}

void loop() {
  int val1 = analogRead(sensor1Pin);
  int val2 = analogRead(sensor2Pin);
  Serial.print(val1);
  Serial.print(",");
  Serial.print(val2);
  Serial.print("\n");
}
