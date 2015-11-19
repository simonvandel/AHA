import sqlite3
import sys
import State

connection = None
NUMSTATES = 100

try:
  connection = sqlite3.connect('test.db')
  cursor = connection.cursor()

  print("Main")

  #cursor.execute("CREATE TABLE States(Id INT, Sensor1 INT, Sensor2 INT, Sensor3 INT)")
  #cursor.execute("CREATE TABLE Action(Id INT, Light INT)")
  
  for i in range(0,NUMSTATES):
    sensor1=i
    sensor2 = i
    sensor3 = i
    #sensors = [sensor1,sensor2,sensor3]
    cursor.execute("INSERT INTO States VALUES(?,?,?,?)",(i, sensor1, sensor2, sensor3))
  
  j=0
  while j<NUMSTATES:
    light = j % 2
    cursor.execute("INSERT INTO Action VALUES(?,?)",(i,light))
    
except sqlite3.Error, e:
  print "Error %s:" % e.args[0]
  sys.exit(1)
    
finally:
  connection.close()

