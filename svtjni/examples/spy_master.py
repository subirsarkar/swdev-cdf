import sys
import java.lang
from java.lang import Runtime
java.lang.System.loadLibrary("SvtvmeImpl")
import jsvtvme
from jsvtvme import Board, SvtvmeConstants
from jsvtvme.Board import *
from jsvtvme.SvtvmeConstants import *
from org.omg.CORBA import IntHolder 

state = IntHolder()

def getMaster(index):
   crate = "b0svt0"+str(i)
   spy = Board(crate, 3, SC)

   error = spy.getState(SC_JUMPER_MASTER, state)
   print "Master Status = ", state.value, " for SC in crate ", crate
   spy.closeBoard()

for i in range(8):
   getMaster(i)

