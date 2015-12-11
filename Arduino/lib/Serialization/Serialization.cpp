//
// Created by kafuch on 07-12-15.
//
#include "Serialization.h"

Serialization::Serialization(){

}

void Serialization::Serialize(int *data, unsigned char *result) {
    int i = 0;
    int j = 0;
    for (; i < 2 ; i++, j+=2) {
        result[j] = (char) (data[i] >> 8);
        result[j+1] = (char) data[i];
    }
}

void Serialization::Deserialize(unsigned char *data, int *result){
    int i = 0;
    int j = 0;
    for (; i < 2; ++i, j+=2) {
        result[i] = (int) data[j] << 8;
        result[i] += (int) data[j+1];
    }
}
