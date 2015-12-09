//
// Created by kafuch on 07-12-15.
//

class Serialization{
  public:
    Serialization();
    void Serialize(int *data, unsigned char *result);
    void Deserialize(unsigned char *data, int *result);
};
