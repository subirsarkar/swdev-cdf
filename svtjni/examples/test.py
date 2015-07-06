import java
java.lang.System.loadLibrary("SvtvmeImpl")
from jsvtvme import SvtImpl,AmsImpl,SpyImpl
from org.omg.CORBA import IntHolder

svt = SvtImpl()
spy = SpyImpl()
ams = AmsImpl()
spyH = svt.svtvme_svt_open("b0svt05.fnal.gov",3)
amsH = svt.svtvme_svt_open("b0svt05.fnal.gov",8)

spy.freeze(spyH)       
spy.isFrozen(spyH)  

from jarray import array,zeros  #  Special to JPython to access Java arrays
data = zeros(100,'i')

error = svt.svtvme_svt_spyBufferRead(0x40, 0, 100, data, amsH)
print error
for i in data:
    print "%x" % (i & 0x7fffff)

print 'Second method'
data_n = svt.readSpy(0x40, 0, 100, amsH)
for i in data_n:
    print "%x" % (i & 0x7fffff)

spy.release(spyH)       
spy.isFrozen(spyH)  

error = spy.svtvme_spy_setState(0x40, 1, amsH);

state = IntHolder()
error = spy.svtvme_spy_getState(0x40, state, amsH);
print 'state.value = ', state.value

print 'state = ', spy.getState(0x40, amsH)
print 'Global Freeze = ', spy.globalFreeze(spyH)
print 'Backplane Freeze = ', spy.backplaneFreeze(spyH)
