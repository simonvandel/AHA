unsigned long timeEnd = 0;
char dataRecieved[65] = {0};

void setup() {
  Serial.begin(9600);
}

void loop() {
  char dataSent[64] = {0};
  int i;
  unsigned long timeStart;

  for(i = 0; i < 64; i++){
    if(i > 0){
      dataSent[i-1] = 'A';
    }
    dataSent[i] = '\r';
    memset(dataRecieved, 0, 65);
    Serial.write(dataSent, i);
    delay(10000);
    dataSent[i] = '\0';
    Serial.print(i);
    Serial.print(',');
    if(!strcmp(dataSent, dataRecieved)){
      Serial.print(timeEnd - timeStart);
    } else {
      Serial.print("ERROR");
    }
  }
}

void serialEvent(){
  timeEnd = millis();
  dataRecieved[Serial.readBytesUntil('\r', dataRecieved, 64)] = '\0';
}
