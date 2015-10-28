int getField(char*, int, byte*, int);
int sendQuery(char *, int);

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
  char startResponse[3];
  uint8_t startQuery[3] = {43,43,43};
  
  Serial.write(startQuery, 3);
  while(!Serial.available()){} // Implement timeout
  Serial.readBytes(startResponse, 3);
  startResponse[2] = '\0';
  if(strcmp(startResponse, "OK")){
    return 1;
  }
  char tmpName[nameLength];
  strcpy(tmpName, fieldName);
  tmpName[nameLength] = '\r';
  sendQuery();
  while(!Serial.available());
  Serial.readBytes(responseValue, responseLength + 1);
  responseValue[responseLength] = '\0';
  return 0;
}

int sendQuery(char *inputQuery, int queryLength){
  Serial.write((uint8_t *)fieldName, nameLength + 1);
}
