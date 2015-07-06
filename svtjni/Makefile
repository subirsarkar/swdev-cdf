all: java shlib

java: 
	cd tool; gmake all
	cd jsvtvme; gmake all
	cd jsvtsim; gmake all

shlib:
	cd src; gmake
	cd srcsim; gmake

# Clean

.PHONY   : clean
clean :
	cd tool; gmake clean
	cd jsvtvme; gmake clean
	cd jsvtsim; gmake clean
	cd src; gmake clean
	cd srcsim; gmake clean
