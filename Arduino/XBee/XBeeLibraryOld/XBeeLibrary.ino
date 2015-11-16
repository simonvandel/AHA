#include <XBee.h>

int SendData(XBeeAddress64 addr, char* toSend, int toSendLen) {
  ZBTxRequest package = ZBTxRequest(addr, (uint8_t *)toSend, toSendLen);
  xbee.send(package);
  if(!VerifySendResponse()) {
    xbee.send(package);
    return(VerifySendResponse());
  }
  return(1);
}

int sendData(ddress64 address, char *toSend, int sendLen){
  ZBTxRequest zbTx = ZBTxRequest(addr64, (uint8_t *)toSend, sendLen);
  xbee.send(zbTx);
  if(xbee.readPacket(500)){
    if(xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
      xbee.getResponse().getZBTxStatusResponse(txStatus);
      if (txStatus.getDeliveryStatus() == SUCCESS) {
        return 1;
      }
    }
  }
  return 0;
}
