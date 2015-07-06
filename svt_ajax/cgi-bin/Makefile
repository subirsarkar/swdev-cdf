CXX        = g++
ROOTCFLAGS = $(shell root-config --cflags)
ROOTLIBS   = $(shell root-config --glibs)
CFLAGS     = -O -I. $(ROOTCFLAGS)
LIBS       = -L/usr/lib -lcgicc $(ROOTLIBS)

all: viewer

viewer:
	$(CXX) $(CFLAGS) HistogramViewer.cxx -o viewer.exe $(LIBS)
	chmod a+x viewer.exe
