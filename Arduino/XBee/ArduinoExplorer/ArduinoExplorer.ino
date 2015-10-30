int getField(char*, int, char**);
int sendQuery(char*, int);
int readResponse(char**);

void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  int i;
  for(i = 10; i > 0; i--){
    Serial.print(i);
    Serial.print('\n');
    delay(1000);
  }
  char *Field;
  if(!getField("ATID", 4, &Field)){
    Serial.print(Field);
    Serial.print('\n');    
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  free(Field);
}

void loop(){}

int sendOKResponseQuery(char *query, int queryLength){
  char *response;
  int errorStatus = 0;
  
  if(errorStatus = sendQuery(query, queryLength)){
    return errorStatus;
  }
  
  if(errorStatus = readResponse(&response)){
    free(response);
    return errorStatus;
  }
  
  if (strcmp(response, "OK")){
    errorStatus = 1;
  }
  
  free(response);
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
    return 1;    
  }

  char ATWR[5] = "ATWR";
  ATWR[4] = '\r';

  errorStatus = sendOKResponseQuery(ATWR, 5);

  return errorStatus;
}

int getField(char *fieldName, int nameLength, char **responseValue){    
  int errorStatus = 0;

  if(errorStatus = sendOKResponseQuery("+++", 3)){
    return errorStatus;
  }
  
  char tmpName[nameLength + 1];
  
  strcpy(tmpName, fieldName);
  tmpName[nameLength] = '\r';
  
  if(errorStatus = sendQuery(tmpName, nameLength + 1)){
    return 1;
  }
  
  errorStatus = readResponse(responseValue);
  return errorStatus;
}

int sendQuery(char *inputQuery, int queryLength){
  int i, j;
  for(i = 0; i < 5; i++){
    Serial.write((uint8_t *)inputQuery, queryLength);
    for(j = 0; j < 10; j++){
      if(Serial.available()){
        Serial.print('\n');
        return 0;
      }
      delay(1000);
    }
  }
  return 1;
}

int readResponse(char **responseResult){
  int tmpResponseLength = 1;
  char responseChar;
  char *tmpResponse, *response;
  
  response = (char *)malloc(tmpResponseLength);
  response[0] = '\0';
    
  while((responseChar = Serial.read()) != '\r'){
    if(++tmpResponseLength < 64){
      tmpResponse = response;
      response = (char *)malloc(tmpResponseLength);
      strcpy(response, tmpResponse);
      free(tmpResponse);
      response[tmpResponseLength - 2] = responseChar;
      response[tmpResponseLength - 1] = '\0';
    } else {
      return 1;
    }
  }
  *responseResult = response;
  return 0;
}
