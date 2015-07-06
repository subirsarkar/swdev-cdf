# General Makefile for messages area invoke both Makefile.vxworks of Makefile.unix


UNAME=$(findstring VxWorks, $(SVTMON_DEST_ENV) )

.PHONY: libmessages
     
libmessages:
ifeq ($(UNAME),VxWorks)
	@echo ******** Building VxWorks version  of messages ********
	gmake -f Makefile.vxworks all
else
	@echo ******** Building Linux or Irix version *************** 
	gmake -f Makefile.unix create_link 
	gmake -f Makefile.unix 
	gmake -f Makefile.unix delete_link
endif

