#include <XBee.h>
int sendData(XBeeAddress64 addr, byte* toSend, int toSendLen, XBee xbee);
int getModemStatusResponse(XBee xbee);
int receiveData(XBee *xbee);
int getATField(char *request, XBee xbee, AtCommandResponse *result);
int setATField(char *request, uint8_t *value, uint8_t valueLength, XBee xbee);
