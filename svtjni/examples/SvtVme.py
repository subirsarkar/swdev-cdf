#
# Wrap the Native interface in an OO fasion. Inspired by code developed
# by Federico Cozzi and Bill Ashmanskas. Alternatively, one can use
# the equivalent code implemented in Board.java which is also a 
# further wrapper over the native interface.
#
# Subir Sarkar  2/2001 
#
import sys
import java
try:
  java.lang.System.loadLibrary("SvtvmeImpl")
except:
  print "shared library cannot be loaded, exiting ..."
  sys.exit(0)

from jsvtvme import SvtvmeImpl, SvtvmeConstants
from jsvtvme.SvtvmeImpl import *
from jsvtvme.SvtvmeConstants import *
from jarray import array, zeros  #  Special to JPython to access Java arrays
from org.omg.CORBA import IntHolder

state = IntHolder()

class Board:

  def __init__(self, crate, slot, boardType):
    self.crate = crate        # The 3 parameters for svtvme_openBoard
    self.slot = slot
    self.boardType = boardType
    self.board = SvtvmeImpl.svtvme_openBoard(crate, slot, boardType)  # SVT Board  handle

  def __del__(self):
    SvtvmeImpl.svtvme_closeBoard(self.board)

  def __repr__(self):
    return '[Board of type '+str(self.boardType)+' opened in Crate '+self.crate+' Slot '+str(self.slot)+']'

  def setBoardFlag(self, flag, value):
    return SvtvmeImpl.svtvme_setBoardFlag(self.board, flag, value)

  def getBoardFlag(self, flag):
    err = SvtvmeImpl.svtvme_getBoardFlag(self.board, flag, state)
    return (err, state.value)

  def getBoardType(self):
    return SvtvmeImpl.svtvme_getBoardType(self.board)

  def init(self):
    return SvtvmeImpl.svtvme_init(self.board)

  def setTmode(self):
    return SvtvmeImpl.svtvme_setTmode(self.board)

  def isTmode(self):
    return SvtvmeImpl.svtvme_isTmode(self.board)

  def getState(self, regId):
    err = SvtvmeImpl.svtvme_getState(self.board, regId, state)
    return (err, state.value)

  def setState(self, regId, d):
    return SvtvmeImpl.svtvme_setState(self.board, regId, d)

  def checkState(self, regId, d):
    return SvtvmeImpl.svtvme_checkState(self.board, regId, d)

  def testRegister(self, regId):
    return SvtvmeImpl.svtvme_testRegister(self.board, regid)

  def readFifoMode(self, regId, nw):
    data = zeros(nw, "i")
    err = SvtvmeImpl.svtvme_readFifoMode(self.board, regId, nw, data)
    return (err, data)
    
  def writeFifoMode(self, regId, nw, data):
    return SvtvmeImpl.svtvme_writeFifoMode(self.board, regId, nw, data)

  def readMemory(self, memId, ndata):
    data = zeros(ndata, "i")
    err = SvtvmeImpl.svtvme_readMemory(self.board, memId, ndata, data)
    return (err, data)

  def checkMemory(self, memId, ndata, data, stopFlag):
    err = SvtvmeImpl.svtvme_checkMemory(self.board, memId, ndata, data, stopFlag, state)
    return (err, state.value)

  def testMemory(self, memId, ntimes, stopFlag):
    err = SvtvmeImpl.svtvme_testMemory(self.board, memId, ntimes, stopFlag, state)
    return (err, state.value)

  def writeMemory(self, memId, ndata, data):
    return SvtvmeImpl.svtvme_writeMemory(self.board, memId, ndata, data)

  def readMemoryFragment(self, memId, offset, nw):
    data = zeros(nw, "i")
    err = SvtvmeImpl.svtvme_readMemoryFragment(self.board, memId, offset, nw, data)
    return (err, data)

  def cksumBlock(self, addr, mask, ndata):
    err = SvtvmeImpl.svtvme_cksumBlock(self.board, addr, mask, ndata, state)
    return (err, state.value)

  def writeMemoryFragment(self, memId, offset, data):
    return SvtvmeImpl.svtvme_writeMemoryFragment(self.board, memId, offset, len(data), data)

  def spyCounter(self, spyId):
    return SvtvmeImpl.svtvme_spyCounter(self.board, spyId)

  def isFrozen(self, spyId):
    return SvtvmeImpl.svtvme_isFrozen(self.board, spyId)

  def isWrapped(self, spyId):
    return SvtvmeImpl.svtvme_isWrapped(self.board, spyId)

  def resetSpy(self, spyId):
    return SvtvmeImpl.svtvme_resetSpy(self.board, spyId)

  def readSpyTail(self, spyId, nw=None):
    if nw == None:
      nw = self.spyCounter(spyId)
    data = zeros(nw, "i")
    nvalid = SvtvmeImpl.svtvme_readSpyTail(self.board, spyId, nw, data)
    if nvalid > 0:
      return data

  def sendDataOnce(self, data, speed=FASTER):
    return SvtvmeImpl.svtvme_sendDataOnce(self.board, len(data), data, speed)

  def readWord(self, addr):
    err = SvtvmeImpl.svtvme_readWord(self.board, addr, state)
    return (err, state.value)

  def readWords(self, addr, nw):
    data = zeros(nw, "i")
    err = SvtvmeImpl.svtvme_readWords(self.board, addr, data, nw)
    return (err, data)

  def writeWord(self, addr, word):
    return SvtvmeImpl.svtvme_writeWord(self.board, addr, word)

  def writeWords(self, addr, nw, data):
    return SvtvmeImpl.svtvme_writeWords(self.board, addr, nw, data)

  def isHeld(self):
    return SvtvmeImpl.svtvme_isHeld(self.board)

  def isEmpty(self, fifoId):
    return SvtvmeImpl.svtvme_isEmpty(self.board, fifoId)

  def isLast(self, fifoId, word):
    return SvtvmeImpl.svtvme_isLast(fifoId, word)

  def SpyRamId(spyId):
    return SvtvmeImpl.svtvme_SpyRamId(spyId)  
  
  def readAllSpy(self, spyId, ndata):
    data = zeros(ndata, "i")
    err = SvtvmeImpl.svtvme_readAllSpy(self.board, spyId, ndata, data)
    return (err, data)

  def deltaSpy(spyId, end, start):
    return SvtvmeImpl.svtvme_deltaSpy(spyId, end, start)

  def readFifo(self, fifoId, nw):
    data = zeros(ndata, "i")
    err = SvtvmeImpl.svtvme_readFifo(self.board, fifoId, nw, data)
    return (err, data)

  def readAllFifo(self, fifoId, maxWords):
    data = zeros(maxWords, "i")
    err = SvtvmeImpl.svtvme_readAllFifo(self.board, fifoId, maxWords, data, state)
    return (state.vale, data)

  def testIDPROM(self):
    return SvtvmeImpl.svtvme_testIDPROM(self.board)
