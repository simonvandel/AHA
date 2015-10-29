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
  if(!setField("ATID 3454", 9)){
    for(i = 10; i > 0; i--){
      Serial.print(i);
      Serial.print('\n');
      delay(1000);
    }
    if(!getField("ATID", 4, ATID, 5)){
      Serial.print(ATID);
      Serial.print('\n');
    } else {
      Serial.print("Error");
      Serial.print('\n');
    }
  } else {
    Serial.print("Error");
    Serial.print('\n');
  }
  Serial.print('\n');
  Serial.print("END OF STORY");
}

void loop(){}

int sendOKResponseQuery(char *query, int queryLength){
  char response[3];
  int errorStatus = 0;
  
  if(errorStatus = sendQuery(query, queryLength, response, 3)){
    /* free(response); */
    return errorStatus;
  }
  
  response[2] = '\0';
  if(strcmp(response, "OK")){
    errorStatus = 1;
  }
  
  /* free(response); */
  return errorStatus;
}

int setField(char *command, int commandLength){
  int errorStatus = 0;

  if(errorStatus = sendOKResponseQuery("+++", 3)){
    return errorStatus;
  }

  char tmpCommand[commandLength + 1];
  
  strcpy(tmpCommand, command);
  tmpCommand[commandLength] = '\r';
  
  if(errorStatus = sendOKResponseQuery(tmpCommand, commandLength + 1)){
    /* free(tmpCommand); */
    return 1;    
  }

  char ATWR[5] = "ATWR";
  ATWR[4] = '\r';

  errorStatus = sendOKResponseQuery(ATWR, 5);

  /* free(tmpCommand); */
  return errorStatus;
}

int getField(char *fieldName, int nameLength, char *responseValue, int responseLength){    
  int errorStatus = 0;

  if(errorStatus = sendOKResponseQuery("+++", 3)){
    return errorStatus;
  }
  
  char tmpName[nameLength + 1];
  
  strcpy(tmpName, fieldName);
  tmpName[nameLength] = '\r';
  
  errorStatus = sendQuery(tmpName, nameLength + 1, responseValue, responseLength);
  
  responseValue[responseLength] = '\0';

  /* free(tmpName); */
  return errorStatus;
}

int sendQuery(char *inputQuery, int queryLength, char *response, int responseLength){
  int i, j;
  for(i = 0; i < 5; i++){
    Serial.write((uint8_t *)inputQuery, queryLength);
    for(j = 0; j < 10; j++){
      if(Serial.available()){
        Serial.readBytes(response, responseLength);
        return 0;
      }
      delay(1000);
    }
  }
  return 1;
}
