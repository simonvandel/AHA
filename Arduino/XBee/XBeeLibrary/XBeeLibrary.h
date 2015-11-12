#ifndefine XBeeLibrary_h
#define XBeeLibrary_h

    #include <XBee.h>
    int SendData(XBeeAddress64 addr, char* toSend, int toSendLen);
    int VerifySendResponse();
#endif
