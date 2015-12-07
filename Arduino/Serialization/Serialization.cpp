//
// Created by kafuch on 07-12-15.
//
#include "Serialization.h"
using namespace std;


void Serialize(short *data, char *result) {
    for (int i = 0, j = 0; i < 2 ; i++, j+=2) {
        result[j] = (char) (data[i] >> 8);
        result[j+1] = (char) data[i];
    }
}

void Deserialize(char *data, short *result){
    for (int i = 0, j = 0; i < 2; ++i, j+=2) {
        result[i] = (short) data[j] << 8;
        result[i] += (short) data[j+1];
    }
}
