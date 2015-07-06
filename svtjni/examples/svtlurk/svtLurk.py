#
# svtLurk.py
# bigger, longer and uncut!
# going graphical now...
# Federico Cozzi, 2000-09-13
#
# BUG BUG BUG !!!
# I've just discovered that jpython doesn't call __del__() methods in user-defined classes !!!
# http://www.jpython.org/docs/differences.html
# OK, now I know why my program segfaults...
# ...this time, it's not my fault!
#
import java
from javax.swing import JFrame, JPanel, JLabel, Timer, BorderFactory
from javax.swing import ImageIcon, JButton, ToolTipManager, BoxLayout
from javax.swing import Box, JTextField, JTextArea, JScrollPane, WindowConstants
from pawt import GridBag
from java.awt import GridLayout, BorderLayout, Dimension, Insets
from java.awt.event import ActionListener
from svtClasses import *
import sys
import math
import re

class spyMonitor(JButton, ActionListener):
    # Static data (shared by all instances)
    # I hope this reduces memory/improves speed...
    yellowLED = ImageIcon("GIF/yellow.gif")
    redLED = ImageIcon("GIF/red.gif")
    greenLED = ImageIcon("GIF/green.gif")

    def __init__(self, spy, parentCrate = None):
        JButton.__init__(self)
	self.spy = spy
        self.parentCrate = parentCrate
	self.text = "0"
	self.icon = self.redLED
        self.addActionListener(self)
        self.margin = Insets(1,1,1,1)
        self.focusPainted = 0
        self.borderPainted = 0

    def actionPerformed(self, event):
	if __debug__:
            print self.spy.name
        if self.parentCrate != None:
            self.parentCrate.setMonitoredSpy(self.spy)

    def update(self):
	oldPtrValue = self.spy.ptr.value
	oldWrpValue = self.spy.wrp.value
        self.spy.read()
        # wrap counter
        if oldWrpValue != self.spy.wrp.value:
            self.text = "%d" % self.spy.wrp.value
        # colored LED
        if self.spy.frz.value:
	    icon = self.yellowLED
	elif self.spy.ptr.value == oldPtrValue:
	    icon = self.redLED
	else:
	    icon = self.greenLED
        if self.icon != icon:
            self.icon = icon
        self.toolTipText = "%5.5x" % self.spy.ptr.value

class spyTailMonitor(JPanel, ActionListener):
    length = 10          # Number of words per line    

    def __init__(self, parentCrate = None):
        JPanel.__init__(self)
        self.spy = None
        self.parentCrate = parentCrate
        self.nw = 10     # Number of words (total)
        self.upperPanel = JPanel()
        self.lowerPanel = JPanel()
        self.add(self.upperPanel)
        self.add(self.lowerPanel)
        self.nameLabel = JLabel("n/a")
        self.slotLabel = JLabel("n/a")
        self.textArea = JTextArea("n/a", 1, 70, editable = 0)
        self.scrollPane = JScrollPane(self.textArea)
        self.textField = JTextField(3, actionCommand = "textField",
                                    text = ("%d" % self.nw))
        self.textField.addActionListener(self)
        self.clearButton = JButton("Stop", actionCommand = "button")
        self.clearButton.addActionListener(self)
        self.border = BorderFactory.createEmptyBorder(5,5,5,5)
        self.layout = BoxLayout(self, BoxLayout.Y_AXIS)
        self.upperPanel.layout = BoxLayout(self.upperPanel, BoxLayout.X_AXIS)
        self.lowerPanel.layout = BoxLayout(self.lowerPanel, BoxLayout.X_AXIS)
        self.upperPanel.add(self.clearButton)
	self.upperPanel.add(Box.createRigidArea(Dimension(20,0)))
        self.upperPanel.add(JLabel("Spy:"))
	self.upperPanel.add(Box.createRigidArea(Dimension(5,0)))
        self.upperPanel.add(self.nameLabel)
	self.upperPanel.add(Box.createRigidArea(Dimension(20,0)))
        self.upperPanel.add(JLabel("Slot:"))
	self.upperPanel.add(Box.createRigidArea(Dimension(5,0)))
	self.upperPanel.add(self.slotLabel)
	self.upperPanel.add(Box.createRigidArea(Dimension(20,0)))
        self.upperPanel.add(JLabel("Number of words:"))
	self.upperPanel.add(Box.createRigidArea(Dimension(5,0)))
        self.upperPanel.add(self.textField)
        self.upperPanel.add(Box.createHorizontalGlue())
	self.lowerPanel.add(JLabel("Tail contents:"))
	self.lowerPanel.add(Box.createRigidArea(Dimension(5,0)))
	self.lowerPanel.add(self.scrollPane)
        self.lowerPanel.add(Box.createHorizontalGlue())

    def setSpy(self, spy):
        self.spy = spy
        self.nameLabel.text = spy.name
        self.slotLabel.text = "%d" % spy.board.slot
        self.textArea.text = "n/a"

    def update(self):
        if self.spy != None:
            array = self.spy.readTail(self.nw)
            text = "%6.6x" % array[0]
            for i in range(1, len(array)):  # len(array) = len(array[1:])+1
                if i % 10:
                    text = text + " %6.6x" % array[i]
                else:
                    text = text + "\n%6.6x" % array[i]
            self.textArea.text = text

    def actionPerformed(self, event):
        if event.actionCommand == "button":
            self.spy = None
            self.nameLabel.text = "n/a"
            self.slotLabel.text = "n/a"
            self.textArea.text = "n/a"
            self.textArea.rows = 1
        elif event.actionCommand == "textField":
            self.nw = eval(self.textField.text)
            self.textArea.rows = min([int(math.ceil(self.nw/self.length)), 10])
	    if self.parentCrate != None:
                self.parentCrate.frame.pack()

# boardMonitor is an abstract class (C++ terminology)
class boardMonitor(JPanel):
    def __init__(self, crate, slot, parentCrate):
        JPanel.__init__(self)
	self.bag = GridBag(self)
        # vertical labels
	for y in range(len(self.board.spyShortNameList)):
	    name = self.board.spyShortNameList[y]
	    self.bag.add(JLabel(name), gridx = 0, gridy = y)
        # spy monitors
	self.spyMonitorList = map(lambda x, parentCrate = parentCrate:
                                  spyMonitor(x, parentCrate),
                                  self.board.spyList)
	for i in range(self.board.spyNumber):
	    self.bag.add(self.spyMonitorList[i], gridx = 1, gridy = i)
	# set borders - this line is copied by an online Swing tutorial...
	self.border = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("%s-%d" % (self.board.type, slot)), BorderFactory.createEmptyBorder(5,5,5,5))

    def close(self): # explicit call to destructor - workaround for jpython bug
        self.board.__del__()

    def update(self):
        if __debug__:
            print self.board.slot
	for spyMonitor in self.spyMonitorList:
            spyMonitor.update()

class hbMonitor(boardMonitor):
    def __init__(self, crate, slot, parentCrate = None):
        self.board = hbBoard(crate, slot)
        boardMonitor.__init__(self, crate, slot, parentCrate)

class mrgMonitor(boardMonitor):
    def __init__(self, crate, slot, parentCrate = None):
        self.board = mrgBoard(crate, slot)
        boardMonitor.__init__(self, crate, slot, parentCrate)

class amsMonitor(boardMonitor):
    def __init__(self, crate, slot, parentCrate = None):
        self.board = amsBoard(crate, slot)
        boardMonitor.__init__(self, crate, slot, parentCrate)

class hfMonitor(boardMonitor):
    def __init__(self, crate, slot, parentCrate = None):
        self.board = hfBoard(crate, slot)
        boardMonitor.__init__(self, crate, slot, parentCrate)

class crateMonitor:
    def __init__(self, crate, mainWindow):
	self.crate = crate
        self.monitoredSpy = None
	self.frame = JFrame(crate, windowClosing = (lambda event, self = self, mainWindow = mainWindow: mainWindow.delete(self)))
        self.frame.contentPane.layout = BoxLayout(self.frame.contentPane, BoxLayout.Y_AXIS)
        self.frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
	self.boardPanel = JPanel()
	self.spyPanel = spyTailMonitor(self)
	self.frame.contentPane.add(self.boardPanel)
	self.frame.contentPane.add(self.spyPanel)
	self.boardList = []
	configFile = open(crate + ".config", "r")
	regexp = re.compile("^(\d+)\s+(MRG|AMS|HB|HF)")
	for line in configFile.readlines():
 	    result = regexp.match(line)
	    if result != None:
                try:
                    slot, type = result.groups()
	            slot = eval(slot)
                    if type == "MRG":
                        self.boardList.append(mrgMonitor(crate, slot, self))
                    elif type == "AMS":
                        self.boardList.append(amsMonitor(crate, slot, self))
                    elif type == "HB":
                        self.boardList.append(hbMonitor(crate, slot, self))
                    elif type == "HF":
                        self.boardList.append(hfMonitor(crate, slot, self))
                except SvtError, e:
                    print e.args
        configFile.close()
        for board in self.boardList:
            self.boardPanel.add(board)
	self.frame.pack()
	self.frame.visible = 1

    def setMonitoredSpy(self, spy):
        self.spyPanel.setSpy(spy)

    def update(self):
        if __debug__:
	    print "%s update" % self.crate
	for board in self.boardList:
            board.update()
        self.spyPanel.update()

    def close(self):
        if __debug__:
	    print "Closing %s crate" % self.crate
        for board in self.boardList:
            board.close()

class svtMonitor(ActionListener):
    def __init__(self, delay):
        self.frame = JFrame("SVT Monitor", \
                            windowClosing = lambda event: sys.exit(0))
	self.grid = GridLayout(2, 4)
	self.pane = self.frame.contentPane
	self.pane.layout = self.grid
	self.buttonList = map(lambda x: JButton("b0svt0" + `x`), \
                              [0,7,6,5,1,2,3,4]) # Left to right,
                                                 # top to bottom
	for button in self.buttonList:
            button.actionCommand = button.text
	    button.addActionListener(self)
            self.pane.add(button)
        self.crateMonitorList = []
	self.frame.pack()
	self.frame.visible = 1
	self.timer = Timer(delay, self)
	self.timer.start()

    def actionPerformed(self, event):
        crate = event.getActionCommand()
        if crate == None: # it's the timer
            self.update()
        else:              # the user pushed a button
	    if __debug__:
                print "%s pushed" % crate
	    crateList = map(lambda x, self=self: x.crate, self.crateMonitorList)
	    if not crate in crateList: # Do not open the same crate more than once
                if __debug__:
                    print "%s opening" % crate
	        self.crateMonitorList.append(crateMonitor(event.getActionCommand(), mainWindow = self))

    def delete(self, crate):
        crate.close()
        self.crateMonitorList.remove(crate)
    
    def update(self):
        if __debug__:
            print "big update"
        for crate in self.crateMonitorList:
            crate.update()

#class MainTimer(ActionListener):
#    def actionPerformed(self, event):
#        print event.actionCommand
#        monitor.update()

if __name__ == "__main__":
    __debug__ = 1 # turns on/off stdout messages
    ToolTipManager.sharedInstance().initialDelay = 0
    monitor = svtMonitor(2000)

