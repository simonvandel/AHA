#include <XBee.h>
ZBTxStatusResponse txStatus = ZBTxStatusResponse();
int sendData(XBeeAddress64 address, char *toSend, int sendLen, XBee xbee){
  ZBTxRequest zbTx = ZBTxRequest(address, (uint8_t *)toSend, sendLen);
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
 

