NAME = SendRootHist
UNAME = $(shell uname) 

# No blanks after the name because we add uname

CSUF = cxx
OSUF = o 

INCLUDE_DIR = -I$(SMARTSOCKETS_DIR)/include -I${SVTMON_DIR}/include 
             
INC_PATH = $(INCLUDE_DIR)
VPATH = .:$(INC_PATH) 
vpath %.h $(INC_PATH) 
 
SRCS = $(wildcard src/*.$(CSUF)) 
OBJS = $(patsubst %.$(CSUF), %.$(OSUF), $(SRCS)) 

MAKEDEPEND = $(shell which makedepend)
LIB_DIR =  -L${SVTMON_LD_PATH}

ROOTCFLAGS = $(shell root-config --cflags)
ROOTLIBS   = $(shell root-config --libs)
ROOTGLIBS  = $(shell root-config --glibs)

CXXFLAGS = -g -Wall -DSMART_SOCKS $(ROOTCFLAGS) $(INC_PATH) \
           -I/usr/include/g++-2/ 
LDFLAGS  = -g

$(NAME)_$(UNAME): $(OBJS)
	env CC=g++ rtlink -cxx  $(LDFLAGS) $^ -o $@  $(LIB_DIR) \
        -lrtcxxipc50 -lpthread $(ROOTGLIBS) 

# Create object files 
%.o : %.$(CSUF) 
	env CC=g++ rtlink -cxx -DSMART_SOCKS $(CXXFLAGS) -c $< -o $@ 
 
# makedepend 
depend: $(SRCS:.$(CSUF)=.$(CSUF).dep) 
	cat $(notdir $^) > Makefile.dep 
	-rm -f $(notdir $^) $(patsubst %,%.bak,$(notdir $^)) 
%.dep: 
	touch $(notdir $@) 
	$(MAKEDEPEND) -f$(notdir $@) -- $(CXXFLAGS) -- $* 
include Makefile.dep 

# Ignore errors, continue compilation 
.IGNORE : 
 
# Clean 
.PHONY   : clean 
clean : 
	-rm $(OBJS)  
	-rm $(NAME)_$(UNAME)
