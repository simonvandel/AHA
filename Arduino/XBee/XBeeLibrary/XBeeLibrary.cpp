#include <XBee.h>
#include <XBeeLibrary.h>
int sendATCommand(AtCommandRequest atRequest, XBee xbee, AtCommandResponse *result);

ZBTxStatusResponse txStatus = ZBTxStatusResponse();
ModemStatusResponse msr = ModemStatusResponse();

int getModemStatusResponse(XBee xbee){
  if(xbee.readPacket(250)){
    if(xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
      xbee.getResponse().getModemStatusResponse(msr);
        if (msr.getStatus() == ASSOCIATED) {
        return 0; // Connected to modem: msr.getStatus()
      } else if (msr.getStatus() == DISASSOCIATED) {
        return 1; // Disconnected/No connection to modem: msr.getStatus()
      } else {
        return 2; // Unknown Modem Status: msr.getStatus()
      }
    } else {
      return 3; // Unexpected ApiId: xbee.getResponse().getApiId()
    }
  } else if (xbee.getResponse().isError()) {
    return 4; // Error reading packet. Error code: xbee.getResponse().getErrorCode()
  }
  return 5; // No packet recieved
}

int sendData(XBeeAddress64 address, byte* toSend, int sendLen, XBee xbee){
  ZBTxRequest zbTx = ZBTxRequest(address, (uint8_t *)toSend, sendLen);
  Serial.print("Sending to ");
  Serial.print(address.getMsb());
  Serial.println(address.getLsb());
  xbee.send(zbTx);
  if(xbee.readPacket(250)){
    if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        return 0;
      } else {
        return 1;
      }
    } else {
      return 2;
    }
  }
  return 3;
}

int receiveData(XBee *xbee) {
  if(xbee->readPacket(250)) {
      ZBRxResponse rx = ZBRxResponse();
      xbee->getResponse().getZBRxResponse(rx);
      if(rx.getOption() == ZB_PACKET_ACKNOWLEDGED) {
        return 1;
      } 
  }
  return 0;
}

int getATField(char *request, XBee xbee, AtCommandResponse *result){
  AtCommandRequest atRequest = AtCommandRequest((uint8_t *)request);
  return sendATCommand(atRequest, xbee, result);
}

int setATField(char *request, uint8_t *value, uint8_t valueLength, XBee xbee){
  AtCommandRequest atRequest = AtCommandRequest((uint8_t *)request, value, valueLength);
  AtCommandResponse result = AtCommandResponse();
  return sendATCommand(atRequest, xbee, &result);
}

int sendATCommand(AtCommandRequest atRequest, XBee xbee, AtCommandResponse *result){
  xbee.send(atRequest);
  if (xbee.readPacket(5000)) {
    if (xbee.getResponse().getApiId() == AT_COMMAND_RESPONSE) {
      AtCommandResponse atResponse = AtCommandResponse();
      xbee.getResponse().getAtCommandResponse(atResponse);
      if (atResponse.isOk()) {
        if (atResponse.getValueLength() >= 0) {
          *result = atResponse;
          return 1;
        } else {
          Serial.print("Response of wrong length ");
          Serial.println(atResponse.getValueLength() );
          return 0;
        }
      } 
      else {
        Serial.println("Command return error code: atResponse.getStatus()");
        return 0;
      }
    } else {
      Serial.print("Expected AT response but got ");
      Serial.println(xbee.getResponse().getApiId());
      return 0;
    }   
  } else {
    if (xbee.getResponse().isError()) {
      Serial.println("Error reading packet. Error code: xbee.getResponse().getErrorCode()");
      return 0;
    }
  }
  Serial.println("No response from radio");
  return 0;
}
