#define DATASET 64

typedef struct Result {
  int time;
  int bytesSent;
} Result;

unsigned long timeEnd = 0;
unsigned long timeStart = 0;
char dataRecieved[DATASET] = {0};
char dataSent[DATASET] = {0};
Result results[DATASET];
int i = 0, timeout = 5000;

void setup() {
  pinMode(13, OUTPUT);
  Serial.begin(9600);
  dataSent[0] = '\0';
  i = 0;
  delay(10000);
  timeStart = millis();
  Serial.write((uint8_t *)dataSent, i + 1);
}

void loop() {
  //timeOut();
}

void serialEvent(){
  timeEnd = millis();
  timeout = 5000;
  blinkLED();
  int dataLen = Serial.readBytesUntil('\0', dataRecieved, 64);
  Result tmpResult;
  dataRecieved[dataLen] = '\0';
  tmpResult.bytesSent = dataLen + 1;
  if(!strcmp(dataSent, dataRecieved)){
    tmpResult.time = timeEnd - timeStart;
  } else {
    tmpResult.time = -1;
  }
  results[i] = tmpResult;
  i++;
  if(i < DATASET){
    memset(dataRecieved, 0, DATASET);
    dataSent[i-1] = 'A';
    dataSent[i] = '\0';
    delay(5000);
    timeStart = millis();
    Serial.write((uint8_t *)dataSent, i + 1);
  } else {
    printResult();
    while(1);
  }
}

void printResult(){
  Serial.print('\n');
  for(i = 0; i < DATASET; i++){
    Serial.print(results[i].bytesSent);
    Serial.print(',');
    Serial.print(results[i].time);
    Serial.print('\n');
  }
}

void blinkLED(){
  digitalWrite(13, HIGH);
  delay(250);
  digitalWrite(13, LOW);
}

void timeOut(){
  while(timeout < 0){
    delay(1);
    timeout--;
  }
  results[i].bytesSent = ++i;
  results[i].time = -2;
  memset(dataRecieved, 0, DATASET);
  dataSent[i-1] = 'A';
  dataSent[i] = '\0';
  delay(3000);
  timeout = 5000;
  timeStart = millis();
  Serial.write((uint8_t *)dataSent, i + 1);
}
