#define DATASET 10

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
  Serial.begin(9600);
  dataSent[0] = '\0';
  i = 1;
  delay(1000);
  timeStart = millis();
  Serial.write((uint8_t *)dataSent, i);
}

void loop() {}

void serialEvent(){
  timeEnd = millis();
  int dataLen = Serial.readBytesUntil('\0', dataRecieved, 64);
  Result tmpResult;
  tmpResult.bytesSent = dataLen + 1;
  if(!strcmp(dataSent, dataRecieved)){
    tmpResult.time = timeEnd - timeStart;
  } else {
    tmpResult.time = -1;
  }
  results[i] = tmpResult;
  if(i < DATASET){
    memset(dataRecieved, 0, DATASET);
    dataSent[i-1] = 'A';
    dataSent[i] = '\0';
    Serial.write((uint8_t *)dataSent, i);
  } else {
    printResult();
    while(1);
  }
  i++;
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
