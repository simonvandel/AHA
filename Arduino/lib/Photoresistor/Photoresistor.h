class Photoresistor {
  public:
    Photoresistor(unsigned int pin);
    unsigned int getLightIntensity();
  private:
    unsigned int inputPin;
};
