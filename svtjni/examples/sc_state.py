import sys
from org.omg.CORBA import IntHolder
import jsvtvme
from jsvtvme import Board,SvtvmeConstants
from jsvtvme.Board import *
from jsvtvme.SvtvmeConstants import *

crate = sys.argv[1]
state = IntHolder()
 
spy = Board(crate, 3, SC)

# Jumper status
spy.getState(SC_JUMPER_MASTER, state)
print 'Master enabled: ', state.value

spy.getState(SC_JUMPER_LAST, state)
print 'This board sits at the end of the daisy chain: ', state.value

# G_Bus Input Status
spy.getState(SC_GINIT_IN, state)
print 'G_INIT from upstream: ', state.value

spy.getState(SC_GFREEZE_IN, state)
print 'G_FREEZE from upstream: ', state.value

spy.getState(SC_GERROR_OUT, state)
print 'G_ERROR from downstream: ', state.value

spy.getState(SC_GLLOCK_OUT, state)
print 'G_LLOCK from downStream: ', state.value

spy.getState(SC_GBUS, state)
print 'G_BUS Input status: ', state.value

# SVT_INIT Generation
spy.getState(SC_INIT_FORCE, state)
print 'SVT_INIT is forced to false(0) or true (1): ', state.value

spy.getState(SC_INIT_ON_GINIT, state)
print 'SVT_INIT is generated in response to the G_INIT signal coming from the master: ', state.value

# Backplane Status
spy.getState(SC_BACKPLANE_INIT, state)
print 'Backplane status, SVT_INIT: ', state.value

spy.getState(SC_BACKPLANE_FREEZE, state)
print 'Backplane status, SVT_FREEZE: ', state.value

spy.getState(SC_BACKPLANE_ERROR, state)
print 'Backplane status, SVT_ERROR: ', state.value

spy.getState(SC_BACKPLANE_LLOCK, state)
print 'Backplane status, SVT_LLOCK: ', state.value

spy.getState(SC_BACKPLANE, state)
print 'Backplane status: ', state.value

# G_ERROR Generation
spy.getState(SC_GERROR_FORCE, state)
print 'G_ERROR is forced to false(0) or true (1)', state.value

spy.getState(SC_GERROR_ON_ERROR, state)
print 'G_ERROR is generated in response to SVT_ERROR in the local backplane: ', state.value

spy.getState(SC_GERROR_DRIVEN, state)
print 'G_ERROR is driven true by this board: ', state.value

# G_LLOCK Generation
spy.getState(SC_GLLOCK_FORCE, state)
print 'G_LLOCK is forced to true: ', state.value

spy.getState(SC_GLLOCK_ON_LLOCK, state)
print 'G_LLOCK is generated in response to SVT_LLOCK in the local backplane: ', state.value

spy.getState(SC_GLLOCK_DRIVEN, state)
print 'G_LLOCK is being driven true by this board: ', state.value

# SVT_FREEZE Generation
spy.getState(SC_FREEZE_FORCE, state)
print 'FREEZE Flip-Flop: ', state.value

spy.getState(SC_FREEZE_ON_ERROR, state)
print 'Enable SVT_ERROR: ', state.value

spy.getState(SC_FREEZE_ON_LLOCK, state)
print 'Enable SVT_LLOCK: ', state.value

spy.getState(SC_FREEZE_ON_GFREEZE, state)
print 'Enable G_FREEZE: ', state.value

# SVT_FREEZE Delay
spy.getState(SC_FREEZE_DELAY, state)
print 'SVT_FREEZE Delay in steps of 1 microsec: ', state.value

# LEVEL1 Counter
spy.getState(SC_LEVEL1COUNTER, state)
print 'LEVEL1 Counter: ', state.value

# CDF_ERROR Generation
spy.getState(SC_CDFERR_FORCE, state)
print 'CDF_ERROR Flip-Flop: ', state.value

spy.getState(SC_CDFERR_ON_ERROR, state)
print 'Enable SVT_ERROR: ', state.value

spy.getState(SC_CDFERR_ON_LLOCK, state)
print 'Enable SVT_LLOCK: ', state.value

spy.getState(SC_CDFERR_ON_GERROR, state)
print 'Eneble G_ERROR(Master only): ', state.value

spy.getState(SC_CDFERR_ON_GLLOCK, state)
print 'Enable G_LLOCK(Master only): ', state.value

# CDF_RECOVER and CDF_RUN Status
spy.getState(SC_CDFRECOV, state)
print 'CDF_RECOVER: ', state.value

spy.getState(SC_CDFRUN, state)
print 'CDF_RUN: ', state.value

# Master registers
# G_INIT Generation
spy.getState(SC_GINIT_FORCE, state)
print 'G_INIT is forced true .....:', state.value

spy.getState(SC_GINIT_ON_CDFSIGS, state)
print 'GINIT from CDFsignals: ', state.value

spy.getState(SC_GINIT_DRIVEN, state)
print 'G_INIT is being driven true: ', state.value

# G_FREEZE Generation
spy.getState(SC_GFREEZE_FORCE, state)
print 'G_FREEZE Flip-Flop: ', state.value

spy.getState(SC_GFREEZE_ON_GERROR, state)
print 'Enable G_ERROR: ', state.value

spy.getState(SC_GFREEZE_ON_GLLOCK, state)
print 'Enable G_LLOCK: ', state.value

# G_FREEZE Delay
spy.getState(SC_GFREEZE_DELAY, state)
print 'G_FREEZE delay in steps of 1 microsec: ', state.value

#spy.closeBoard()
