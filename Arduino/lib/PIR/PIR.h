#define NUM_READINGS 10

class PIR {
  public:
    PIR(unsigned int tp);
    bool getMotionDetected();
  private:
    unsigned int triggerPin;
};
