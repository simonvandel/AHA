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
int i = 0;

void setup() {
  pinMode(13, OUTPUT);
  Serial.begin(9600);
  dataSent[0] = '\0';
  i = 0;
  delay(10000);
  timeStart = millis();
  Serial.write((uint8_t *)dataSent, i + 1);
  blinkLED();
}

void loop() {}

void serialEvent(){
  timeEnd = millis();
  blinkLED();
  int dataLen = Serial.readBytesUntil('\0', dataRecieved, 64);
  Result tmpResult;
  tmpResult.bytesSent = dataLen;
  if(!strcmp(dataSent, dataRecieved)){
    tmpResult.time = timeEnd - timeStart;
  } else {
    tmpResult.time = -1;
  }
  results[i] = tmpResult;
  if(i < DATASET){
    i++;
    memset(dataRecieved, 0, DATASET);
    dataSent[i-1] = 'A';
    dataSent[i] = '\0';
    delay(2000);
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

