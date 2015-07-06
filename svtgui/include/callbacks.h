#ifndef SVTGUI_CALLBACK_H
#define SVTGUI_CALLBACK_H
/* 
 * Declare all the event callback function prototypes
 */
#include <gnome.h>
#include "svtvme_public.h"

enum 
{ 
  SPY_LENGTH_ANY  = 131072,
  MAX_FIFO_LENGTH = 1000,
  MAX_MEM_LENGTH  = 1000,
  MAX_STRING_LEN  = 256,
  MAX_REG_ELEM    = 150,
  MAX_MEM_ELEM    = 100,
  MAX_SPY_ELEM    = 20,
  MAX_FIFO_ELEM   = 20,
  MAX_SLOTS       = 21
};

void svtgui_initApplication();
GtkWidget *svtgui_appNew(const gchar *crate, const gchar *board, 
                         gint slot, gint connect, 
                         const gchar *geometry);
void svtgui_appClose(GtkWidget *app);

/* Menu callbacks */
void svtgui_outputNameCB(const gchar *fname, gpointer data);
void svtgui_scriptNameCB(const gchar *fname, gpointer data);
void svtgui_showDocCB(GtkMenuItem *item, gpointer data);
void svtgui_clearMessageCB(GtkMenuItem *item, gpointer data);
void svtgui_newCB(GtkMenuItem *item, gpointer data);
void svtgui_saveCB(GtkMenuItem *item, gpointer data);
void svtgui_saveAsCB(GtkMenuItem *item, gpointer data);
void svtgui_openConfigCB(GtkMenuItem *item, gpointer data);
void svtgui_saveConfigCB(GtkMenuItem *item, gpointer data);
void svtgui_closeCB(GtkMenuItem *item, gpointer data);
void svtgui_exitCB(GtkMenuItem *item,  gpointer data);
void svtgui_noImpCB(GtkWidget* widget, gpointer data);

void svtgui_verboseModeCB(GtkMenuItem *item, gpointer data);
void svtgui_wordsModeCB(GtkMenuItem *item, gpointer data);
void svtgui_boardFormatCB(GtkMenuItem *item, gpointer data);
void svtgui_hexFormatCB(GtkMenuItem *item, gpointer data);
void svtgui_decimalFormatCB(GtkMenuItem *item, gpointer data);

void svtgui_aboutCB(GtkMenuItem *item, gpointer data);

/* Button push event callback */
void svtgui_buttonClickCB(GtkButton *button, gpointer data);
void svtgui_browseCB(GtkButton *button, gpointer data);
void svtgui_clearRegCB(GtkButton *button, gpointer data);
void svtgui_sendFreezeCB(GtkButton *button, gpointer data);

void svtgui_validateCrateCB(GtkWidget *combo_entry, GtkSelectionData *data, 
		    guint info, guint time, gpointer combo);
void svtgui_validateBoardCB(GtkWidget *combo_entry, GtkSelectionData *data, 
		    guint info, guint time, gpointer combo);
/* Radiobutton toggle callback */
void svtgui_radioButtonToggleCB(GtkToggleButton *togglebutton, gpointer data);

/* The following are application specific callbacks */
void svtgui_selectBoardCB(GtkWidget *widget, GtkSelectionData *data, guint info,
                                   guint time, gpointer combo);
void svtgui_selectSlotCB(GtkWidget *combo_entry, GtkSelectionData *data, 
                                   guint info, guint time, gpointer combo);
void svtgui_mapCrateCB(GtkButton *button, gpointer data);

/* Python related */
void assertInterpreter(void);
void svtgui_startPython(void);
void svtgui_shutPython(void);
void svtgui_initPython(void);
void svtgui_runScript(gpointer data);
void svtgui_execScriptCB(GtkButton *button, gpointer data);
void svtgui_saveScript(gpointer data);

void svtgui_noImplementation(void);
void svtgui_selectBoard(GtkWidget *widget);
void svtgui_selectSlot(GtkWidget *combo_entry);

void svtgui_setRegisters();
void svtgui_setMemories();
void svtgui_setSpies();
void svtgui_setFifos();
void svtgui_resetClock(GtkWidget *clock);
void svtgui_setAllAddresses();
void svtgui_setAllDefaults();

void svtgui_openBoard(const gchar *crate_name, const gchar *board_name, gint slot_num);
void svtgui_closeBoard(svtvme_h board);
gint svtgui_ensureBoardType(gint board_type);
void svtgui_ensureOpenBoard(const gchar *crate_name, const gchar *board_name, gint slot_num);
void svtgui_initBoard(void);
void svtgui_readTmode(void);
void svtgui_readHold(void);
gint svtgui_setTmode(gboolean mode);
void svtgui_readSpyStatus(void);
void svtgui_readSpyBuffer();
void svtgui_readFifoStatus();
void svtgui_readFifo();
void svtgui_sendData();
void svtgui_awordOperation();
void svtgui_testMemory();
void svtgui_memoryOperation(void);
void svtgui_showData(GtkText *widget, const gchar *name, gint ndata, uint4 *data);
void svtgui_resetSpy(void);
void svtgui_readStatus(gint board_type);

void svtgui_readBoardRegister(gint index);
void svtgui_writeBoardRegister(gint index);
void svtgui_clearBoardRegister(GtkWidget *widget);
void svtgui_testBoardRegister(gint index);
void svtgui_readErrorReg(gint board_type);
gint svtgui_mapCrate(const gchar *crate, gint *slot_list,
		     gchar **board_list, gint *sernum_list);
gint svtgui_mapBoard(const gchar *crate, const gchar *board, gint *slot_list);
gint svtgui_getIdprom(svtvme_h board, gchar *serial, gchar *version);

#endif
