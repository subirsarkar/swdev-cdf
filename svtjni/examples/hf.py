import startup
from startup import *

crate  = sys.argv[1]
slot   = int(sys.argv[2])
nWords = int(sys.argv[3])

hf  = Board(crate, slot, HF)
spy = Board(crate, 3, SC)

data = zeros(nWords,'i')
state = IntHolder()

spy.freeze()
for i in range(11):
  if i == 10:
      reg = 'HF_OUT_SPY'
  else:
      reg = 'HF_ISPY_' + str(i)
  print 'isFrozen(hf,' + reg + ') =>',  hf.isFrozen(eval(reg)), '/',
  print 'isWrapped(hf,' + reg + ') =>', hf.isWrapped(eval(reg)), '/',
  print 'spyCounter(hf,'+ reg + ') =>', hf.spyCounter(eval(reg))
  error = hf.readSpyTail(eval(reg), nWords, data)
  if data:
      if i < 10:
          data = filter(lambda x: (x & 0xf000) != 0xc000, data)
          data = map(lambda x: x & 0xffff, data)
          print_hex(data, '4.4')
      else:
          print_hex(data)
spy.release()
spy.closeBoard();
hf.closeBoard();
