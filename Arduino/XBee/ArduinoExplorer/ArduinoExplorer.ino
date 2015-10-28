int getField(char*, int, byte*, int);
int sendQuery(char*, int, char*);

void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  int i;
  for(i = 10; i > 0; i--){
    Serial.print(i);
    Serial.print('\n');
    delay(1000);
  }
  char ATID[5];
  if(!getField("ATID", 4, ATID, 4)){
    Serial.print("Success! ");
    Serial.print(ATID);
  } else {
    Serial.print("Error");
  }
}

void loop(){}

int getField(char *fieldName, int nameLength, char *responseValue, int responseLength){  
  char startQuery[4] = "+++";
  char startResponse[3];
  
  int errorStatus = 0;
  
  if(errorStatus = sendQuery(startQuery, 3, startResponse, 2)){
    return errorStatus;
  }
  
  startResponse[2] = '\0';
  if(strcmp(startResponse, "OK")){
    return 1;
  }
  
  free(startQuery);
  free(startResponse);
  
  char tmpName[nameLength + 1];
  
  strcpy(tmpName, fieldName);
  tmpName[nameLength] = '\r';
  
  if(errorStatus = sendQuery(tmpName, nameLength + 1, responseValue, responseLength)){
    return errorStatus;
  }
  
  responseValue[responseLength] = '\0';
  return 0;
}

int sendQuery(char *inputQuery, int queryLength, char *response, int responseLength){
  int i, j;
  for(i = 0; i < 5; i++){
    Serial.write((uint8_t *)inputQuery, queryLength);
    for(j = 0; j < 20; j++){
      if(Serial.available()){
        Serial.readBytes(response, responseLength);
        return 0;
      }
      delay(1000);
    }
  }
  return 1;
}
