void setup(){
  // Start up our serial port, we configured our XBEE devices for 9600 bps.
  Serial.begin(9600);
  printPANID();
}

void loop(){}

int getField(char *fieldName, int nameLength, char* responseValue){  
  int i;
  for(i = 10; i > 0; i--){
    Serial.print(i);
    Serial.print('\n');
    delay(1000);
  }
  char response[5];
  Serial.write("+++", 3);
  while(!Serial.available());    // Implement timeout
  Serial.readBytes(response, 2);
  response[2] = '\0';
  if(strcmp(response, "OK")){
    return 1;
  }
  responseValue = malloc((nameLength + 1) * sizeof(char));
  fieldName[nameLength] = '\r';
  Serial.write(fieldName, nameLength);
  while(!Serial.available());
  Serial.readBytes(response, 5);
  Serial.print(response);
  Serial.print('\n');
}

