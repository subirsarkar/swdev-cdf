import math
print 'Board Test Mode: ', b.isTmode()
al = []
for i in [MRG_A_SPY, MRG_B_SPY, MRG_C_SPY, MRG_D_SPY, MRG_OUT_SPY]:
  print b.isFrozen(i), b.isWrapped(i), b.spyCounter(i)
  if b.isFrozen(i):
    print b.readSpyTail(i, 10)
  else:
    print 'WARNING: buffer ', i, ' is not frozen!'
    
