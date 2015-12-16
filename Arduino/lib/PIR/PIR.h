#define NUM_READINGS 10

class PIR {
  public:
    PIR(unsigned int tp);
    bool getMotionDetected();
  private:
    unsigned int readings[NUM_READINGS];
    unsigned int readIndex;
    float average;
    unsigned int total;
    unsigned int triggerPin;
};
