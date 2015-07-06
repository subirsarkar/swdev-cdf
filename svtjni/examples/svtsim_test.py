from getmod import *
import sys, string

def getEvents(fnam):
  print "Read data from file: ", fnam
  events = []
  event  = []

  input = open(fnam, "r")
  lines = input.readlines()
  lines = map(lambda x: x[:-1], lines)
  for x in lines:
    try:
      v = eval("0x"+ x)
    except ValueError:
      continue
    event.append(v)
    if (v & 0x600000) == 0x600000:
      events.append(event)
      event = []
      
  input.close()
  
  return events

def getData():
  data = []

  input = open("/cdf/people2/sarkar/work/dump/anal/hit_word_0.dat", "r")
  lines = input.readlines()
  lines = map(lambda x: x[:-1], lines)
  words = string.split(string.join(lines))
  #nums  = map(lambda x: string.atoi(x, 16), words)
  for i in words:
    word = eval("0x" + i)
    print "0x%6.6x" % word
    data.append(word)
    if (word & 0x600000 == 0x600000): break
  input.close()
  return data
  
def printData(data, label="Cable"):
  print label
  for i in data:
    print "0x%x" % i
  
def main(): 
  wedge = 0
  nlay  = 6
  mapsetName = "bmorg_1_2mm_wedgefit_20020213170044"
  map = WedgeMaps()
  ams = SimAMS(wedge)
  hb  = SimHB(nlay)
  tf  = SimTF(wedge)

  map.initFromMapSet(wedge, mapsetName, 4043633698L, "/cdf/people2/sarkar/work/svtmon/", 1)

  # ams.setUcode(SVTSIM_AMS_UCODE_4_OUT_OF_4)   # already done in the constructor
  ams.useMaps(map)
  hb.useMaps(map)
  tf.useMaps(map)

  # AM Sequencer
  ams_cable_in = Cable()
  
  try:
    events = getEvents("/cdf/people2/sarkar/work/dump/anal/hit_word_0.dat")
  except IOError:
    print "unable to open", fnam, "for input"
    raise

  for ev in events:
    ams_cable_in.copyWords(ev)
    ams.plugInput(ams_cable_in)
    ams.procEvent()

    ams_cable_out = Cable(ams.outputCable())
    out = ams_cable_out.data()
    printData(out, "AMS Output")

    # Hit Buffer
    hb.plugHitInput(ams_cable_in)
    hb.plugRoadInput(ams_cable_out)
    hb.procEvent()
    
    hb_cable_out = Cable(hb.outputCable())
    out = hb_cable_out.data()
    printData(out, "HB Output")
  
    # Track Fiter
    tf.plugInput(hb_cable_out)
    tf.procEvent()

    tf_cable_out = Cable(tf.outputCable())
    out = tf_cable_out.data()
    printData(out, "TF Output")
  
if __name__=="__main__": 
  main() 

