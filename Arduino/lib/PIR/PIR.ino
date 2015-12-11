#define LED 13
#define PIR_SIGNAL 8
#define TIER1_SAMPLES_TO_TAKE INT_MAX
#define TIER2_SAMPLES_TO_TAKE 5
#include <limits.h>

void setup() {
  Serial.begin(9600);
  pinMode(LED, OUTPUT);
  pinMode(PIR_SIGNAL, INPUT);
}
int val = LOW;
int tier1SamplesTaken = 0;
int tier1HighSamples = 0;
int tier1LowSamples = 0;
int tier2SamplesTaken = 0;
int tier2HighSamples = 0;
int tier2LowSamples = 0;

// to reduce random noise in the data,
// we collect TIER1_SAMPLES_TO_TAKE samples,
// each sample measuring how many were low or high.
// We do this TIER2_SAMPLES_TO_TAKE times.
// At last we see if the most samples were high or low.
// If the most were high, we report motion, and opposite otherwise.
void loop() {
  val = digitalRead(PIR_SIGNAL);
  tier1SamplesTaken += 1;
  if(tier1SamplesTaken >= TIER1_SAMPLES_TO_TAKE) {
    tier2SamplesTaken += 1;
    if(tier1HighSamples > tier1LowSamples) {
      tier2HighSamples += 1;
    }
    else {
      tier2LowSamples += 1;
    }
    
    resetTier1Counters();

    if(tier2SamplesTaken >= TIER2_SAMPLES_TO_TAKE) {
      if(tier2HighSamples > tier2LowSamples) {
        Serial.println("HIGH");
      }
      else{
        Serial.println("LOW");
      }
      resetTier2Counters();
    }
  }
  if(val == HIGH)
  {
    tier1HighSamples += 1;
    
  }
  else
  {
     tier1LowSamples += 1;
  }
}

void resetTier1Counters() {
  tier1SamplesTaken = 0;
  tier1HighSamples = 0;
  tier1LowSamples = 0;
}

void resetTier2Counters() {
  tier2SamplesTaken = 0;
  tier2HighSamples = 0;
  tier2LowSamples = 0;
}

