int getField(char*, int, char**);
int setField(char*);
int sendQuery(char*, int);
int readResponse(char**);

int const ledpin = 13;
int const butpin = 12;

void testBaud(){
  int i;
  char *Field;
  int serials[8] = {1200,2400,4800,9600,19200,38400,57600,115200};
  for(i = 0; i < 8; i++){
    // Start up our serial port, we configured our XBEE devices for 9600 bps.
    Serial.begin(serials[i]);
    if(!setField("ATBD 3")){
      Serial.end();
      Serial.begin(9600);
      Serial.print("SUCCESS! Baud: ");
      Serial.print(serials[i]);
      Serial.print('\n');
      Serial.end();
      Serial.begin(serials[i]);
    }else{
      Serial.end();
      Serial.begin(9600);
      Serial.print("ERROR");
      Serial.print('\n');
      Serial.end();
      Serial.begin(serials[i]);
    }
    Serial.end();
  }
  Serial.begin(9600);
  Serial.print('\n');
  Serial.print("END");
  free(Field);
}

void printXBeeConfig(){
  char *field;
  if(!initiateCommandMode()){
    Serial.print("SUCCESS");
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(!getField("ATCH", 4, &field)){
    Serial.print(field);
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(!getField("ATID", 4, &field)){
    Serial.print(field);
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(!getField("ATSL", 4, &field)){
    Serial.print(field);
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(!getField("ATSH", 4, &field)){
    Serial.print(field);
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(!getField("ATDD", 4, &field)){
    Serial.print(field);
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(!getField("ATDH", 4, &field)){
    Serial.print(field);
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(!getField("ATDL", 4, &field)){
    Serial.print(field);
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(!getField("ATCE", 4, &field)){
    Serial.print(field);
    Serial.print('\n');
  }else{
    Serial.print("ERROR");
    Serial.print('\n');
  }
}

void setup(){
  pinMode(ledpin, OUTPUT);
  pinMode(butpin, INPUT);
  Serial.begin(9600);
  printXBeeConfig();
}

void loop(){}

int copySLSH(){
  int i;
  Serial.end();
  Serial.begin(9600);
  char *ATSL0;
  char *ATSH0;
  char *ATSL1;
  char *ATSH1;

  Serial.print("Insert module");
  Serial.print('\n');
  while(!digitalRead(butpin));
  initiateCommandMode();

  if(getField("ATSL", &ATSL0)){
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(getField("ATSH", &ATSH0)){
    Serial.print("ERROR");
    Serial.print('\n');
  }
  
  Serial.print("Change module");
  Serial.print('\n');
  while(!digitalRead(butpin));
  initiateCommandMode();
  
  if(getField("ATSL", &ATSL1)){
    Serial.print("ERROR");
    Serial.print('\n');
  }
  if(getField("ATSH", &ATSH1)){
    Serial.print("ERROR");
    Serial.print('\n');
  }
  char command1[10] = "ATDL ";
  strcat(command1, ATSL0);
  if(setField(command1)){
    Serial.print("ERROR");
    Serial.print('\n');
  }
  strcpy(command1, "ATDH ");
  strcat(command1, ATSH0);
  if(setField(command1)){
    Serial.print("ERROR");
    Serial.print('\n');
  }
  
  Serial.print("Change module");
  Serial.print('\n');
  while(!digitalRead(butpin));
  initiateCommandMode();
  
  char command2[10] = "ATDL ";
  strcpy(command2, "ATDL ");
  strcat(command2, ATSL1);
  if(setField(command2)){
    Serial.print("ERROR");
    Serial.print('\n');
  }
  strcpy(command2, "ATDH ");
  strcat(command2, ATSH1);
  if(setField(command2)){
    Serial.print("ERROR");
    Serial.print('\n');
  }
}

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

int initiateCommandMode(){
  return sendOKResponseQuery("+++", 3);
}

int setField(char *command){
  int errorStatus = 0;
  int commandLength = strlen(command);
  
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

int getField(char *fieldName, char **responseValue){    
  int errorStatus = 0;
  int nameLength = strlen(fieldName);
  
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
