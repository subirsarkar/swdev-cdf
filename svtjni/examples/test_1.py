from getmod import *
from org.omg.CORBA import StringHolder

state = StringHolder()

ams = Board("b0svt05", 8, AMS)
error = Board.objectName(AMS_HSPY_PTR, state)
print state.value


