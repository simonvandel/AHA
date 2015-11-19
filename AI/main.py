import Pattern
import riak

PATTERN_SIZE = 6
NUMSTATES = 1000

def main():
  patterns = list(Pattern())
  states = list(State())
  actions = list(Action())
  print("trying to connect")
  #Open database
  riakClient = riak.RiakClient(pb_port=8098)
  print("connected")
  
  #Open buckets or create if not existing
  stateBucket = riakClient.bucket('states')
  actionBucket = riakClient.bucket('actions')
  #Iterbuckets by index
  for i in range(0,NUMSTATES):
    states.append = stateBucket.get(i).data # or state['time'] index
    actions.append = actionBucket.get(i).data
    print(actionBucket.get(i).data)

  for s in states:
    statesBeforeS = states[s-PATTERN_SIZE:s]
    actionAtTimeS = actions[s.time]
    patterns.append(makePattern(statesBeforeS,PATTERN_SIZE,actionAtTimeS))

  #Open pattern bucket or create if not existing, store by time
  patternBucket = riakClient.bucket('patterns')
  for i in range(0,NUMSTATES):
    newPattern = patternBucket.new(patterns['time'], data=patterns[i])
    newPattern.store()
    print("store")
  
