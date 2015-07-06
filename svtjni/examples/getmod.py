import sys
import java
java.lang.System.loadLibrary("SvtvmeImpl")
from org.omg.CORBA import IntHolder
import jsvtvme
from jsvtvme import Board, SvtvmeConstants
from jsvtvme.Board import *
from jsvtvme.SvtvmeConstants import *
import jsvtsim
from jsvtsim import SimAMS,SimHB,SimTF,Dict,Cable,WedgeMaps
from jsvtsim.SimAMS import *
from jsvtsim.SimHB import *
from jsvtsim.SimTF import *
from jsvtsim.Dict import *
from jsvtsim.Cable import *
from jsvtsim.WedgeMaps import *
from jarray import array,zeros  #  Special to JPython to access Java arrays
from time import asctime, localtime, time, sleep
