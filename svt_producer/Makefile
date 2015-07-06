UNAME = $(shell uname) 

# No blanks after the name because we add uname
NAME = SvtHistogramProducer

CSUF = cxx
OSUF = o 

CONSUMER_DIR = /data1/consumer/DisplayServer 
INCLUDE_DIR = -I${SMARTSOCKETS_DIR}/include \
              -I$(CONSUMER_DIR) \
              -I./include
             
INC_PATH = $(INCLUDE_DIR)
VPATH = .:$(INC_PATH) 
vpath %.h $(INC_PATH) 
vpath %.c src 
 
SRCS = $(wildcard src/*.$(CSUF)) 
OBJS = $(patsubst %.$(CSUF), %.$(OSUF), $(SRCS)) 

MAKEDEPEND = $(shell which makedepend)
LIB_DIR =  -L$(CONSUMER_DIR) -L${SMARTSOCKETS_DIR}/lib/

ROOTCFLAGS = $(shell root-config --cflags)
ROOTLIBS   = $(shell root-config --libs)
ROOTGLIBS  = $(shell root-config --glibs)

CXXFLAGS = -g -Wall -DSMART_SOCKS $(ROOTCFLAGS) $(INC_PATH) -I/usr/include/g++-2/ \
           -fno-rtti -fno-exceptions
LDFLAGS  = -g

$(NAME)_$(UNAME): $(OBJS)
	env CC=egcs rtlink -cxx  $(LDFLAGS) $^ -o $@  $(LIB_DIR) \
        -lDisplayServer -lrtcxxipc50 -lpthread $(ROOTGLIBS) 

# Create object files 
%.o : %.$(CSUF) 
	env CC=egcs rtlink -cxx $(CXXFLAGS) -c $< -o $@ 
 
# makedepend 
depend: $(SRCS:.$(CSUF)=.$(CSUF).dep) 
	cat $(notdir $^) > Makefile.dep 
	-rm -f $(notdir $^) $(patsubst %,%.bak,$(notdir $^)) 
%.dep: 
	touch $(notdir $@) 
	$(MAKEDEPEND) -f$(notdir $@) --$(CXXFLAGS) -- $* 
include Makefile.dep 

# Ignore errors, continue compilation 
.IGNORE : 
 
# Clean 
.PHONY   : clean 
clean : 
	-rm $(OBJS)  
	-rm $(NAME)_$(UNAME)
