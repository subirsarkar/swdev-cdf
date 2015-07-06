import startup
from startup import *

# Define class snapshot and implement single and multiple shots as detailed
# by Luciano.
# Version 0.1
# Subir Sarkar  10/05/2000

# Board name, number of boards in a crate and the slot number put in a dictionary
board_attr = {
  'HF':[4,5,6,13,14,15],
  'MG':[7,16],
  'AMS':[8,17],
  'HB':[12,21],
  'SC':[3]
}
master_crate = "b0svt06"

class Snapshot:
  "Take snapshots of spy buffers "
  def __init__(self,crate='b0svt01', nword=100, nwait=60, nloop=10):
    self.crate = crate
    self.nword = nword  
    self.nwait = nwait
    self.nloop = nloop
    self.helper = Startup(self.crate)
    if self.crate != master_crate:
      self.master = Board("b0svt06", 3, SC)
    else:
      self.master = self.helper.bMap['S3']
      
  def __del__(self):                           # Destructor 
    self.helper.closeBoards()       

  def openBoards(self):
    self.helper.openBoards()

  def closeBoards(self):
    self.helper.closeBoards()

  def setGFreeze(self):   
    state = IntHolder()
    error = self.helper.bMap['S3'].getState(SC_FREEZE_ON_GFREEZE, state)
    if not state.value:
      self.helper.bMap['S3'].write(SC_FREEZE_ON_GFREEZE, 1)
    self.master.write(SC_GFREEZE_FORCE, 1)
   
  def clearGFreeze(self):   
    self.master.write(SC_GFREEZE_FORCE, 0)

  def readL1Counter(self):
    return self.helper.bMap['S3'].read(SC_LEVEL1COUNTER)

  def clearL1Counter(self):
    self.helper.bMap['S3'].write(SC_LEVEL1COUNTER, 0)

  def resetFlags(self):
    boards = board_attr.keys()
    boards.sort()
    for board in boards:
      if board == 'HF':
        for slot in board_attr[board]:
          handle = self.helper.bMap['S'+str(slot)]
          # inputs
          for i in range(10):
            name = eval("HF_ISPY_"+str(i))
            self.helper.handle.resetSpy(name)
            self.helper.handle.write(eval("HF_ISPY_"+str(i)+"_WRP"), 0)
            # Output
            self.helper.handle.resetSpy(HF_OUT_SPY)
            self.helper.handle.write(HF_OSPY_WRP, 0)
      elif board == 'MG':
        for slot in board_attr[board]:
          handle = self.helper.bMap['S'+str(slot)]
          for tag in ('A', 'B', 'C', 'D', 'OUT'):
            name = eval("MRG_"+ tag + "_SPY")
            self.helper.handle.resetSpy(name)
            self.helper.handle.write(eval("MRG_"+tag[0]+"SPY_WRP"),0)
      elif board == 'AMS':
        for slot in board_attr[board]:
          handle = self.helper.bMap['S'+str(slot)]
          for tag in ('HIT', 'OUT'):
            name = eval("AMS_"+ tag + "_SPY")
            self.helper.handle.resetSpy(name)
            self.helper.handle.write(eval("AMS_"+tag[0]+"SPY_WRP"), 0)
      elif board == 'HB':
        for slot in board_attr[board]:
          handle = self.helper.bMap['S'+str(slot)]
          for tag in ('HIT', 'ROAD', 'OUT'):
            name = eval("HB_"+ tag + "_SPY")
            self.helper.handle.resetSpy(name)
            self.helper.handle.write(eval("HB_"+tag[0]+"SPY_WRP"), 0)
      elif board == 'SC':
        pass

  def dumpSpy(self):
    boards = board_attr.keys()
    boards.sort()

    for board in boards:
      if board == 'HF':
        print board_attr[board]
        for slot in board_attr[board]:
          handle = self.helper.bMap['S'+str(slot)]
          # inputs
          for i in range(10):
            name = eval("HF_ISPY_"+str(i))
            data = self.helper.handle.readSpy(name, self.nword)
            if data:
              self.header(board, slot, 'I'+str(i))
              beautify(data, ' DA')
          # Output
          name = HF_OUT_SPY
          data = self.helper.handle.readSpy(name, self.nword)
          if data:
            self.header(board, slot, 'O1')
            beautify(data, ' DA')
      elif board == 'MG':
        for slot in board_attr[board]:
          handle = self.helper.bMap['S'+str(slot)]
          for tag in ('A', 'B', 'C', 'D', 'OUT'):
            name = eval("MRG_"+ tag + "_SPY")
            if tag == 'B':     sp = 'I1'
            elif tag == 'C':   sp = 'I2'
            elif tag == 'D':   sp = 'I3'
            else:              sp = 'O1'
            data = self.helper.handle.readSpy(name, self.nword)
            if data:
              self.header(board, slot, sp)
              beautify(data, ' DA')
      elif board == 'AMS':
        for slot in board_attr[board]:
          handle = self.helper.bMap['S'+str(slot)]
          for tag in ('HIT', 'OUT'):
            name = eval("AMS_"+ tag + "_SPY")
            if tag == 'HIT':  sp = 'I1'
            else:             sp = 'O1'
            data = self.helper.handle.readSpy(name, self.nword)
            if data:
              self.header(board, slot, sp)
              beautify(data, ' DA')
      elif board == 'HB':
        for slot in board_attr[board]:
          handle = self.helper.bMap['S'+str(slot)]
          for tag in ('HIT', 'ROAD', 'OUT'):
            name = eval("HB_"+ tag + "_SPY")
            if   tag == 'HIT':    sp = 'I1'
            elif tag == 'ROAD':   sp = 'I2'
            else:                 sp = 'O1'
            data = self.helper.handle.readSpy(name, self.nword)
            if data:
              self.header(board, slot, sp)
              beautify(data, ' DA')
      elif board == 'SC':
        pass

  def header(self, board_name, slot, buffer_id):
    print ' SB', board_name, self.crate, slot, buffer_id, self.nword

  def snap_header(self, iloop):
    (year, month, day, hour, minute, seconds,a,b,c) = localtime(time())
    now = str(year)[2:] + '/' + str(month) + '/' + str(day) \
          + '/' + str(hour) + '/' + str(minute) + '/' + str(seconds)
    print ' NS', iloop, now, self.readL1Counter()
      
  def release(self):
    self.helper.bMap['S3'].release()
  
  def take_snap(self, n):       # Single Shot steps
    while 1:
      self.setGFreeze()         # Assert Global freeze by the Master Spy
      if self.readL1Counter():  # Read Level 1 Counter, if not wait and read again 
        break                   # got Level 1 Counter 
      sleep(self.nwait/12)
    self.snap_header(n)
    self.dumpSpy()              # Read and dump spy buffers   
    self.resetFlags()           # Reset Spy pointer and wrap flags
    self.clearL1Counter()       # Reset Level 1 Counter
    self.clearGFreeze()         # Clear Global freeze
    self.release()              # Clear local freeze thereafter
    self.master.release()
  
if __name__ == '__main__':
  crate = sys.argv[1]
  nw    = int(sys.argv[2])      # Number of words
  nwait = int(sys.argv[3])      # Now wait for this many seconds
  nloop = int(sys.argv[4])      # Read nloop times

  snap = Snapshot(crate, nw, nwait, nloop)
  snap.openBoards()
  for i in range(nloop):
    snap.take_snap(i+1)
    sleep(nwait)
  snap.closeBoards()       
