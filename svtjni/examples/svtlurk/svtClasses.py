import java
java.lang.System.loadLibrary("SvtvmeImpl")
from jsvtvme import SvtvmeImpl
from jsvtvme.SvtvmeImpl import *
from jsvtvme.SvtvmeConstants import *
import jarray
from org.omg.CORBA import IntHolder

impl = SvtvmeImpl()

import exceptions
class SvtError(exceptions.Exception):
    def __init__(self, args = None):
        self.args = args

class Spy:
                  # spy name    ptr name      frz name      wrp name     short 
    spyMapping = [
                  # AMS
                  [AMS_HIT_SPY, AMS_HSPY_PTR, AMS_HSPY_FRZ, AMS_HSPY_WRP, "H"],
                  [AMS_OUT_SPY, AMS_OSPY_PTR, AMS_OSPY_FRZ, AMS_OSPY_WRP, "O"],
                  # Hit Buffer
                  [HB_HIT_SPY , HB_HSPY_PTR , HB_HSPY_FRZ , HB_HSPY_WRP , "H"],
                  [HB_ROAD_SPY, HB_RSPY_PTR , HB_RSPY_FRZ , HB_RSPY_WRP , "R"],
                  [HB_OUT_SPY , HB_OSPY_PTR , HB_OSPY_FRZ , HB_OSPY_WRP , "O"],
                  # Merger
                  [MRG_A_SPY  , MRG_ASPY_PTR, MRG_ASPY_FRZ, MRG_ASPY_WRP, "A"],
                  [MRG_B_SPY  , MRG_BSPY_PTR, MRG_BSPY_FRZ, MRG_BSPY_WRP, "B"],
                  [MRG_C_SPY  , MRG_CSPY_PTR, MRG_CSPY_FRZ, MRG_CSPY_WRP, "C"],
                  [MRG_D_SPY  , MRG_DSPY_PTR, MRG_DSPY_FRZ, MRG_DSPY_WRP, "D"],
                  [MRG_OUT_SPY, MRG_OSPY_PTR, MRG_OSPY_FRZ, MRG_OSPY_WRP, "O"],
                  # Hit Finder
                  [HF_ISPY_0  , HF_ISPY_0_PTR, HF_ISPY_FRZ, HF_ISPY_0_WRP,"0"],
                  [HF_ISPY_1  , HF_ISPY_1_PTR, HF_ISPY_FRZ, HF_ISPY_1_WRP,"1"],
                  [HF_ISPY_2  , HF_ISPY_2_PTR, HF_ISPY_FRZ, HF_ISPY_2_WRP,"2"],
                  [HF_ISPY_3  , HF_ISPY_3_PTR, HF_ISPY_FRZ, HF_ISPY_3_WRP,"3"],
                  [HF_ISPY_4  , HF_ISPY_4_PTR, HF_ISPY_FRZ, HF_ISPY_4_WRP,"4"],
                  [HF_ISPY_5  , HF_ISPY_5_PTR, HF_ISPY_FRZ, HF_ISPY_5_WRP,"5"],
                  [HF_ISPY_6  , HF_ISPY_6_PTR, HF_ISPY_FRZ, HF_ISPY_6_WRP,"6"],
                  [HF_ISPY_7  , HF_ISPY_7_PTR, HF_ISPY_FRZ, HF_ISPY_7_WRP,"7"],
                  [HF_ISPY_8  , HF_ISPY_8_PTR, HF_ISPY_FRZ, HF_ISPY_8_WRP,"8"],
                  [HF_ISPY_9  , HF_ISPY_9_PTR, HF_ISPY_FRZ, HF_ISPY_9_WRP,"9"],
                  [HF_OUT_SPY , HF_OSPY_PTR  , HF_OSPY_FRZ, HF_OSPY_WRP  ,"O"]
                 ]

    def __init__(self, board, name):
	self.board = board
	self.name = name
	self.id = eval(name)
        for spy in self.spyMapping:
            if spy[0] == self.id:  # Found!
                self.ptrName = spy[1]
                self.frzName = spy[2]
                self.wrpName = spy[3]
	        self.shortName = spy[4]
                break
        self.ptr = IntHolder()
        self.frz = IntHolder()
        self.wrp = IntHolder()

    def read(self):
        self.ptr = self.board.getState(self.ptrName)
        self.frz = self.board.getState(self.frzName)
        self.wrp = self.board.getState(self.wrpName)

    def readTail(self, nw):
        return self.board.readSpyTail(self.id, nw)

class Board:
# Uncomment the following three commented lines to add some checking.
# I don't need it, since I know I'm going to open only valid crates.
#    crateList = map(lambda x: "b0svt0%d" % x, range(8))

    def __init__(self, crate, slot):
#        if not crate in self.crateList:
#            raise SvtError, "%s is not a valid crate" % crate
        self.crate = crate
        self.slot = slot
        self.spyList = map(lambda spyId, self = self: Spy(self, spyId),
                           self.spyIdList)
        self.spyShortNameList = map(lambda spy: spy.shortName, self.spyList)
	self.spyNumber = len(self.spyList)
	self.handle = impl.svtvme_openBoard(crate, slot, eval(self.type))
	if self.handle == 0:
            raise SvtError, "Can't open crate %d, slot %d" % (crate, slot)

    def __del__(self):
        impl.svtvme_closeBoard(self.handle)

    def getState(self, regId):
	data = IntHolder()
	impl.svtvme_getState(self.handle, regId, data)
	return data

    def readAllSpy(self):
        for spy in self.spyList:
            spy.read()

    def readSpyTail(self, spyId, nw):
        data = jarray.zeros(nw, 'i')
	impl.svtvme_readSpyTail(self.handle, spyId, nw, data)
	return data

class mrgBoard(Board):
    def __init__(self, crate, slot):
        self.type = "MRG"
        self.spyIdList = ["MRG_A_SPY", "MRG_B_SPY", "MRG_C_SPY", "MRG_D_SPY", 
                          "MRG_OUT_SPY"]
        Board.__init__(self, crate, slot)

class amsBoard(Board):
    def __init__(self, crate, slot):
        self.type = "AMS"
        self.spyIdList = ["AMS_OUT_SPY", "AMS_HIT_SPY"]
        Board.__init__(self, crate, slot)

class hbBoard(Board):
    def __init__(self, crate, slot):
        self.type = "HB"
        self.spyIdList = ["HB_OUT_SPY", "HB_ROAD_SPY", "HB_HIT_SPY"]
        Board.__init__(self, crate, slot)

class hfBoard(Board):
    def __init__(self, crate, slot):
        self.type = "HF"
        self.spyIdList = ["HF_OUT_SPY", "HF_ISPY_0", "HF_ISPY_1", "HF_ISPY_2",
                          "HF_ISPY_3", "HF_ISPY_4", "HF_ISPY_5", "HF_ISPY_6",
                          "HF_ISPY_7", "HF_ISPY_8", "HF_ISPY_9"]
        Board.__init__(self, crate, slot)
