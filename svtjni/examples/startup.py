import sys
import java
from org.omg.CORBA import IntHolder

import jsvtvme
from jsvtvme import Board, SvtvmeConstants
from jsvtvme.Board import *
from jsvtvme.SvtvmeConstants import *

from jarray import array,zeros  #  Special to Jython to access Java arrays
from time import asctime, localtime, time, sleep

def print_hex(data, fm='6.6'):
  for i in range(len(data)):
    format = "%"+fm+"x"
    if (i == 0) or (i % 10):
      print format % data[i],
    else:
      print "\n"+format % data[i],
  print "\n"

# Prints  the spy buffer in hex format, 10 words a line starting with 'DA'
def beautify(data, tag):  
  print tag,
  for i in range(1,len(data)):
    print "%6.6x" % data[i-1],
    if not i%10:
      print "\n", tag,
  print
  
class Startup:
   def __init__(self, crate='b0svt05.fnal.gov'):
      self.crate = crate

   def __del__(self):
      self.closeBoards()

   def openBoards(self):
      self.bMap = {
        'S3': Board(self.crate, 3, SC),
        'S4': Board(self.crate, 4, HF),
        'S5': Board(self.crate, 5, HF),
        'S6': Board(self.crate, 6, HF),
        'S7': Board(self.crate, 7, MRG),
        'S8': Board(self.crate, 8, AMS),
       'S12': Board(self.crate, 12, HB),
       'S13': Board(self.crate, 13, HF),
       'S14': Board(self.crate, 14, HF),
       'S15': Board(self.crate, 15, HF),
       'S16': Board(self.crate, 16, MRG),
       'S17': Board(self.crate, 17, AMS),
       'S21': Board(self.crate, 21, HB)
      }

   def closeBoards(self):
     for key in self.bMap.keys():
        if (self.bMap[key]):
           self.bMap[key].closeBoard()

   def testLoop(self, svtB, spyId, nWords=10, ntimes=10):
     if (svtB):
       data = zeros(nWords, 'i')
       memId = svtB.SpyRamId(spyId)
       print 'MemId = ', memId 
       for i in range(ntimes):
         error = svtB.readMemoryFragment(memId, i, nWords, data)
         print 'Offset = ', i, ' error = ', error
         if data: print_hex(data)

   def inLoop(self, svtB, reg, time=10, n=10):
      if (svtB):
        for i in range(n):
          print svtB.read(reg)
          sleep(time)

   def header(self, slot, board_name, spy_buffer, nWords):
      print '==', self.crate + '.' + "%2.2d" % str(slot), '/', board_name, \
            '/', spy_buffer, '/', asctime(localtime(time())), '/', str(nWords)

   def spyReadLoop(self, nWords=10, nsec=60, ntimes=1):
      for j in range(ntimes):
        self.bMap['S3'].freeze()    #  Freeze the Spy system
        
        # Hit Finder 
        hf_obj = { 
           4:self.bMap['S4'],   5:self.bMap['S5'],   6:self.bMap['S6'],
          13:self.bMap['S13'], 14:self.bMap['S14'], 15:self.bMap['S15']
        }
        slots = hf_obj.keys()
        slots.sort()
        for slot in slots:
          for i in range(10):                         # Input Spy
            spy_buffer = "HF_ISPY_" + str(i)
            self.header(slot, 'HF', spy_buffer, nWords)
            reg = eval(spy_buffer)
            data = hf_obj[slot].readSpy(reg, nWords)
            if data:
              data = filter(lambda x: (x & 0xf000) != 0xc000, data)
              data = map(lambda x: x & 0xffff, data)
              print_hex(data, '4.4')
          # output
          self.header(slot, 'HF', 'HF_OUT_SPY', nWords)
          data = hf_obj[slot].readSpy(HF_OUT_SPY, nWords)
          if data:
            print_hex(data)

        # Merger
        mrg_obj = { 7:self.bMap['S7'], 16:self.bMap['S16'] } 
        slots = mrg_obj.keys()
        slots.sort()
        for slot in slots:
          for tag in ['A', 'B','C','D', 'OUT']:            # Input and output Spy
            spy_buffer = "MRG_" + tag + "_SPY"
            self.header(slot, 'MERGER', spy_buffer, nWords)
            reg = eval(spy_buffer)
            data = mrg_obj[slot].readSpy(reg, nWords)
            if data: 
               print_hex(data)

        # AMS
        ams_obj = {8:self.bMap['S8'], 17:self.bMap['S17']}
        slots = ams_obj.keys()
        slots.sort()
        for slot in slots:
          for tag in ['HIT','OUT']:                  # Input and output Spy
            spy_buffer = "AMS_" + tag + "_SPY"
            self.header(slot, 'AMS', spy_buffer, nWords)
            reg = eval(spy_buffer)
            data = ams_obj[slot].readSpy(reg, nWords)
            if data: 
              print_hex(data)

        # Hit Buffer
        hb_obj = {12:self.bMap['S12'], 21:self.bMap['S21']}
        slots = hb_obj.keys()
        slots.sort()
        for slot in slots:
          for tag in ['HIT','ROAD','OUT']:            # Input and output Spy
            spy_buffer = "HB_" + tag + "_SPY"
            self.header(slot, 'HB', spy_buffer, nWords)
            reg = eval("HB_"+tag+"_SPY")
            data = hb_obj[slot].readSpy(reg, nWords)
            if data: 
              print_hex(data)

        self.bMap['S3'].release()    # once all the data is read release freeze
        if j == ntimes-1:
          break
        sleep(nsec)             # Then sleep for n sec before reading again

if __name__ == '__main__':
  __debug__ = 0 # turns on/off stdout messages
  st = Startup(sys.argv[1])
  st.openBoards()
  st.spyReadLoop(int(sys.argv[2]), int(sys.argv[3]), int(sys.argv[4]))
  st.closeBoards()

    
