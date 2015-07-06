/*
 * Build the user interface, the frontend to the svtvme library. 
 * Alex and Subir
 */
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <gnome.h>

#include "config.h"       /* May #define here instead of in the cmd line */
#include "graph_global.h" /* Global UI components are declared here */
#include "callbacks.h"    /* prototypes for all the callbacks */
#include "interface.h"    /* Created by glade */
#include "support.h"      /* convenience functions supplied by gtk+ */
#include "svtvme_public.h"
#include "utils.h"        /* Declaration of maps and prototypes for fucntions */
			  /* which manipulate the maps */

/* Use button width and height consistently */
enum {
  BUTTON_WIDTH  = 75,
  BUTTON_HEIGHT = 25,
  TEXT_HEIGHT   = 10
};

static gchar *crate_name[] = {"tstsvt1.fnal.gov",
			      "b0svt00.fnal.gov", "b0svt01.fnal.gov", "b0svt02.fnal.gov", 
                              "b0svt03.fnal.gov", "b0svt04.fnal.gov", "b0svt05.fnal.gov", 
                              "b0svt06.fnal.gov", "b0svt07.fnal.gov",
			      "vmesvt1.ts.infn.it"};
static gchar *board_name[] = {"SC", "HF", "MRG", "AMS", "AMB", "HB",
			      "TF", "XTFA", "XTFB", "XTFC", "GB"};
static gchar *slot_name[]  = { "3",  "4",  "5",  "6",  "7",  "8",  "9", "10", 
                             "11", "12", "13", "14", "15", "16", "17", "18", 
                             "19", "20", "21"};
static gchar *null_array[] = {""};
static gchar *speed_opt[]  = {"FASTER", "SLOWER"};


/* 
 * Gnome convenience for menu building, there cannot be any
 * forward declaration (as far as I understand) as C does not
 * support that. So first build all the menuitems, then menus
 * and finally attach them to a menubar which is further palced
 * in the main window.
 */

/* File Menu */
static GnomeUIInfo file_menu[] =
{
  /* GNOMEUIINFO_MENU_NEW_ITEM (N_("_New Window"), " Open a new window ...", svtgui_newCB, NULL), */
  {
    GNOME_APP_UI_ITEM, N_("_Save Output as ..."),
    " Save contents of the output area in a file previously used ...",
    svtgui_saveAsCB, NULL, NULL,
    GNOME_APP_PIXMAP_STOCK, GNOME_STOCK_PIXMAP_SAVE,
    0, 0, NULL
  },
  {
    GNOME_APP_UI_ITEM, N_("_Clear Output Area ..."),
    " Clear content of the output area ...",
    svtgui_clearMessageCB, NULL, NULL,
    GNOME_APP_PIXMAP_NONE, NULL,
    0, 0, NULL
  },
  GNOMEUIINFO_SEPARATOR,
  /* GNOMEUIINFO_MENU_CLOSE_WINDOW_ITEM (svtgui_closeCB, NULL), */
  GNOMEUIINFO_MENU_EXIT_ITEM (svtgui_exitCB, NULL),
  GNOMEUIINFO_END
};

/* 
 * Sub Menu within setting 
 */
static GnomeUIInfo data_menu[] =
{
  GNOMEUIINFO_RADIOITEM("Hexadecimal ", " Show data in hexadecimal format ...", 
                         svtgui_hexFormatCB, NULL),
  GNOMEUIINFO_RADIOITEM("Board Format", " Show data in board format ...", 
                         svtgui_boardFormatCB, NULL),
  GNOMEUIINFO_END
};

static GnomeUIInfo radiolist_menu[] = {
  GNOMEUIINFO_RADIOLIST(data_menu),
  GNOMEUIINFO_END
};

/* 
 * Setting Menu 
 */
static GnomeUIInfo setting_menu[] =
{
  GNOMEUIINFO_TOGGLEITEM("Multi-word line", 
                "Assume Multi-word line for I/O...", svtgui_wordsModeCB, NULL),
  GNOMEUIINFO_TOGGLEITEM("Verbose Mode", 
                " Turn on/off verbose mode ...", svtgui_verboseModeCB, NULL),
  GNOMEUIINFO_SUBTREE("Data Format", radiolist_menu),
  GNOMEUIINFO_END
};

/* 
 * Help Menu 
 */
static GnomeUIInfo help_menu[] =
{
  {
    GNOME_APP_UI_ITEM, N_("Help Topic"),
    " Display svtgui documentation ...",
    svtgui_showDocCB, NULL, NULL,
    GNOME_APP_PIXMAP_NONE, NULL,
    GDK_R, GDK_CONTROL_MASK, NULL
  },
  GNOMEUIINFO_MENU_ABOUT_ITEM (svtgui_aboutCB, NULL),
  GNOMEUIINFO_END
};

/* 
 * Finally the complete menu which is attached to the menubar 
 */
static GnomeUIInfo menubar[] =
{
  GNOMEUIINFO_MENU_FILE_TREE (file_menu),
  GNOMEUIINFO_MENU_SETTINGS_TREE (setting_menu),
  GNOMEUIINFO_MENU_HELP_TREE (help_menu),
  GNOMEUIINFO_END
};

/* 
 * Create and initialize a toolbar 
 */
static GnomeUIInfo toolbar[] =
{
  GNOMEUIINFO_ITEM_STOCK("Save", "Save contents of the Output area to the recently selected file",
			 svtgui_saveCB, GNOME_STOCK_PIXMAP_SAVE),
  GNOMEUIINFO_ITEM_STOCK("Save As", "Save contents of the Output area as",
			 svtgui_saveAsCB, GNOME_STOCK_PIXMAP_SAVE_AS),
  GNOMEUIINFO_END
};

/* Structure which holds all the global components */
GlobalComponent svt_gc;

/* 
 * A huge function which creates the full interface. 
 */
void svtgui_createMainWindow (GtkWidget *app) {
  int i;
  gchar name[100];
  gchar tag[6];
  GtkWidget *hbox_send_button;
  GtkWidget *vbox_send_button;
  GtkWidget *hbox_legend;
  GtkWidget *hbox_leg_white;
  GtkWidget *hbox_leg_green;
  GtkWidget *hbox_leg_yellow;
  GtkWidget *hbox_leg_red;
  GtkWidget *hbox_leg_gray;
  GtkWidget *hbox_display_mem;
  GtkWidget *hbox_fifo_1;
  GtkWidget *hbox_fifo_2;
  GtkWidget *label_spy_wrap;
  GtkWidget *label_spy_freeze;
  GtkWidget *hbox_idprom_entry;
  GtkWidget *frame_send_option;
  GtkWidget *frame_data_source;
  GtkWidget *vbox_send_option;
  GtkWidget *hbox_send_option;
  GtkWidget *vbox_data_source;
  GtkWidget *hbox_data_source;

  GtkWidget *label_init_static;
  GtkWidget *frame_mem_pix;
  GtkWidget *hbox_pixs_hold;
  GtkWidget *hbox_pixs_test;
  GtkWidget *hbox_pixs_fifo;
  GtkWidget *button_fifo_status;
  GtkWidget *table_send_data;
  GtkWidget *label_tab_send_data;
  GtkWidget *label_fifo1;
  GtkWidget *label_fifo2;
  GtkWidget *read_fifo_frame;
  GtkWidget *table_fifo_2;
  GtkWidget *label_fifo8;
  GSList *fifo_radio_group = NULL;
  GSList *send_data_group = NULL;
  GSList *send_source_group = NULL;
  GtkWidget *button_fifo_read;
  GtkWidget *legend_fifo_frame;

  GtkWidget *top_level_v_box;
  GtkWidget *combo_h_box;
  GtkWidget *notebook_widget[2];
  GtkWidget *table_global;
  GtkWidget *label_test_mode;
  GtkWidget *label_hold;
  GtkWidget *label_fifo;
  GtkWidget *frame_hold;
  GtkWidget *frame_fifo_status;
  GtkWidget *frame_tmode;
  GtkWidget *button_init;
  GtkWidget *frame4;
  GtkWidget *hbox4;
  GtkWidget *label12;
  GtkWidget *label11;
  GtkWidget *label16;
  GtkWidget *labelx;
  GtkWidget *hbox11;
  GSList *address_group = NULL;
  GtkWidget *button_add_go;
  GtkWidget *hbox13;
  GtkWidget *label17;
  GtkWidget *hbox14;
  GtkWidget *button_refresh;
  GtkWidget *button_send_data;
  GtkWidget *label_tab_global;
  GtkWidget *hbox8;
  GtkWidget *button_mem_test;
  GtkWidget *label15;
  GtkWidget *hbox10;
  GSList *read_mem_group = NULL;
  GtkWidget *button_mem_go;
  GtkWidget *hbox6;
  GtkWidget *label13;
  GSList *mem_display_group = NULL;
  GtkWidget *hbox15;
  GtkWidget *label_tab_memory;
  GSList *num_w_group = NULL;
  GtkWidget *label_spy_num_words;
  GtkWidget *button_spy_read;
  GSList *file_group = NULL;
  GtkWidget *label_spy_pointer;
  GtkWidget *send_label_speed;
  GtkWidget *button_spy_reset;
  GtkWidget *vbox1;
  GtkWidget *button_spy_status_read;
  GtkWidget *hbox_wrap;
  GtkWidget *hbox_freeze;
  GtkWidget *label_tab_spy_buffer;

  GtkWidget *button_read_reg[5];
  GtkWidget *button_write_reg[5];
  GtkWidget *button_read_reg_all;
  GtkWidget *button_write_reg_all;
  GtkWidget *button_clear_reg_all;
  GtkWidget *button_test_reg_all;

  GtkWidget *label1;
  GtkWidget *label2;
  GtkWidget *vbox6;
  GtkWidget *hbox16;
  GtkWidget *button7;
  GtkWidget *message_area_frame;
  GtkWidget *data_area_frame;
  GtkWidget *scrolled_window_1;
  GtkWidget *scrolled_window_2;
  GtkWidget *scrolled_window_3;
  GtkTooltips *tooltips;
  GSList *num_word_group = NULL;
  GSList *num_word_fifo_group = NULL;

  GtkWidget *label_message_tab;
  GtkWidget *label_data_tab;
  GtkWidget *label_script_tab;

  GtkWidget *combo_entry_crate;
  GtkWidget *combo_entry_board;

  GtkWidget *sc_freeze_label;
  GtkWidget *sc_freeze_entry;
  GtkWidget *sc_freeze_button;
  GtkWidget *sc_release_button;

  GtkWidget *python_hbox;
  GtkWidget *python_fileentry;
  GtkWidget *python_run_label;
  GtkWidget *python_exec_button;
  GtkWidget *python_save_button;

  GtkWidget *label_reg_x[NUM_REGS];
  GtkWidget *reg_le_hbox[NUM_REGS];

  GtkWidget *button_crate_map;

  gchar combo_reg_str[32];
  gchar hbox_reg_str[32]; 
  gchar entry_reg_str[32]; 
  gchar read_reg_str[32];
  gchar write_reg_str[32];
  gchar label_reg_str[32];

  /* Help message */
  gchar *message = 
    "-> Select crate/board/slot and click any svtvme related button to open the board.\n"
    "-> Once the board is opened if a different crate/board/slot combination is selected \n"
    "     any further svtvme access will automatically close the opened board and open a new one.\n"
    "-> Choose verbose mode from the Settings menu if you want to know what the application "
    "is doing.\n";

  /* using getenv() seems to be required to construct pixmap file name */
  g_snprintf(svt_gc.pix_file_white, NEL(svt_gc.pix_file_white), "%s%s", 
             getenv("SVTVME_DIR"), "/svtgui/images/white-ball.xpm");
  g_snprintf(svt_gc.pix_file_green, NEL(svt_gc.pix_file_green), "%s%s", 
             getenv("SVTVME_DIR"), "/svtgui/images/green-ball.xpm");
  g_snprintf(svt_gc.pix_file_red, NEL(svt_gc.pix_file_red), "%s%s", 
             getenv("SVTVME_DIR"), "/svtgui/images/red-ball.xpm");
  g_snprintf(svt_gc.pix_file_yellow, NEL(svt_gc.pix_file_yellow), "%s%s", 
             getenv("SVTVME_DIR"), "/svtgui/images/yellow-ball.xpm");
  g_snprintf(svt_gc.pix_file_gray, NEL(svt_gc.pix_file_gray), "%s%s", 
             getenv("SVTVME_DIR"), "/svtgui/images/gray-ball.xpm");
  tooltips = gtk_tooltips_new ();  /* Initialise tooltip */

  svt_gc.statusbar_app = gnome_appbar_new(FALSE, TRUE, GNOME_PREFERENCES_NEVER);
  gnome_app_set_statusbar(GNOME_APP(app), svt_gc.statusbar_app);
  svtgui_installMenusAndToolbar(app);

  /* The main application area */
  top_level_v_box = gui_createVbox(app, "top_level_v_box", 0, 0);
  gnome_app_set_contents (GNOME_APP(app), top_level_v_box); 

  /* The combo boxes for Crate/board/slot */
  combo_h_box = gui_createHbox(app, "combo_h_box", 10, 4);
  gtk_box_pack_start (GTK_BOX (top_level_v_box), combo_h_box, FALSE, FALSE, 0);

  svt_gc.combo_crate = gui_createCombo(app, "combo_crate", 150, BUTTON_HEIGHT, 
                                            0, NEL(crate_name), crate_name);
  gtk_combo_disable_activate (GTK_COMBO(svt_gc.combo_crate));

  svt_gc.combo_board = gui_createCombo(app, "combo_board", 100, BUTTON_HEIGHT, 
                                            0, NEL(board_name), board_name);
  gtk_combo_disable_activate (GTK_COMBO(svt_gc.combo_board));

  svt_gc.combo_slot = gui_createCombo(app, "combo_slot", 65, BUTTON_HEIGHT, 
                                            0, NEL(slot_name), slot_name);
  gtk_combo_disable_activate (GTK_COMBO(svt_gc.combo_slot));

  button_crate_map = gui_createButton(app, "MapCrate", 
                    "button_crate_map",  100, 40, 4);

  button_refresh = gnome_stock_button(GNOME_STOCK_PIXMAP_REFRESH);
  gtk_widget_set_name (button_refresh, "button_refresh");
  gtk_widget_ref (button_refresh);
  gtk_object_set_data_full (GTK_OBJECT (app), "button_refresh", button_refresh,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button_refresh);
  gtk_widget_set_usize (button_refresh, 100, 40);
  gtk_container_set_border_width (GTK_CONTAINER (button_refresh), 5);

  gtk_box_pack_start (GTK_BOX (combo_h_box), svt_gc.combo_crate, FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (combo_h_box), svt_gc.combo_board, FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (combo_h_box), svt_gc.combo_slot,  FALSE, FALSE, 0);
  gtk_box_pack_end   (GTK_BOX (combo_h_box), button_refresh,     FALSE, FALSE, 0);
  gtk_box_pack_end   (GTK_BOX(combo_h_box),  button_crate_map,   FALSE, FALSE, 0);

  /* Now create a notebook and add several pages to it */
  notebook_widget[0] = gui_createNotebook(app, "notebook_widget[0]") ;
  gtk_box_pack_start (GTK_BOX (top_level_v_box), notebook_widget[0], FALSE, FALSE, 0);

  /* 
   * Page Global
   */
  table_global = gui_createTable(app, "table_global", 8, 6, -1, -1, 3, 1, 10);
  gtk_container_add (GTK_CONTAINER (notebook_widget[0]), table_global);

  /* Row 1 */
  label_init_static = gui_createLabel(app, "Init", "label_init_static", 60, BUTTON_HEIGHT);
  frame4            = gui_createFrame(app, NULL, "frame4", 120, 26);
  button_init       = gui_createButton(app, "Init", "button_init", BUTTON_WIDTH, BUTTON_HEIGHT, 0);

  gtk_table_attach (GTK_TABLE (table_global), label_init_static, 0, 1, 0, 1,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), frame4, 1, 2, 0, 1,
                   (GtkAttachOptions) (GTK_EXPAND), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), button_init, 5, 6, 0, 1,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  hbox4 = gui_createHbox(app, "hbox4", 0, 5);
  gtk_container_add (GTK_CONTAINER (frame4), hbox4);
  gtk_widget_set_usize (hbox4, 60, 26);

  svt_gc.pixmap_init = gui_createIcon(app, "pixmap_init", svt_gc.pix_file_gray); 
  svt_gc.label_init  = gui_createLabel(app, "Waiting", "label_init", 60, BUTTON_HEIGHT);
  gtk_label_set_justify (GTK_LABEL (svt_gc.label_init), GTK_JUSTIFY_RIGHT);

  gtk_box_pack_start (GTK_BOX (hbox4), svt_gc.pixmap_init, FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox4), svt_gc.label_init, TRUE, FALSE, 0);

  /* Row 2 */
  label_test_mode         = gui_createLabel(app, "TMode", "label_test_mode", 60, BUTTON_HEIGHT);
  frame_tmode             = gui_createFrame(app, NULL, "frame_tmode", 120, 26);
  svt_gc.button_test_mode = gui_createButton(app, "Enable", "button_test_mode", 
                                                   BUTTON_WIDTH, BUTTON_HEIGHT, 0);

  gtk_table_attach (GTK_TABLE (table_global), label_test_mode, 0, 1, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), frame_tmode, 1, 2, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), svt_gc.button_test_mode, 5, 6, 1, 2,
                   (GtkAttachOptions) (0),  (GtkAttachOptions) (0), 0, 0);

  hbox_pixs_test = gui_createHbox(app, "hbox_pixs_test", 0, 5);
  gtk_container_add (GTK_CONTAINER (frame_tmode), hbox_pixs_test);
  gtk_widget_set_usize (hbox_pixs_test, 100, BUTTON_HEIGHT);

  svt_gc.pixmap_tmode = gui_createIcon(app, "pixmap_tmode", svt_gc.pix_file_gray); 
  svt_gc.label_tmode =  gui_createLabel(app, "Waiting", "label_tmode", 90, BUTTON_HEIGHT);
  gtk_label_set_justify (GTK_LABEL (svt_gc.label_tmode), GTK_JUSTIFY_RIGHT);

  gtk_box_pack_start (GTK_BOX (hbox_pixs_test), svt_gc.pixmap_tmode, FALSE, FALSE, 0);
  gtk_box_pack_end (GTK_BOX (hbox_pixs_test), svt_gc.label_tmode, TRUE, FALSE, 0);

  /* Row 3 */
  label_hold = gui_createLabel(app, "Hold", "label_hold", 60, BUTTON_HEIGHT);
  frame_hold = gui_createFrame(app, NULL, "frame_hold", 120, 26);

  gtk_table_attach (GTK_TABLE (table_global), label_hold, 0, 1, 2, 3,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), frame_hold, 1, 2, 2, 3,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  hbox_pixs_hold = gui_createHbox(app, "hbox_pixs_hold", 0, 5);
  gtk_container_add (GTK_CONTAINER (frame_hold), hbox_pixs_hold);

  svt_gc.pixmap_hold = gui_createIcon(app, "pixmap_hold", svt_gc.pix_file_gray); 
  gtk_box_pack_start (GTK_BOX (hbox_pixs_hold), svt_gc.pixmap_hold, FALSE, FALSE, 0);

  svt_gc.label_hold = gui_createLabel(app, "Waiting", "label_hold", -1, -1);
  gtk_box_pack_end (GTK_BOX (hbox_pixs_hold), svt_gc.label_hold, TRUE, FALSE, 0);
  gtk_label_set_justify (GTK_LABEL (svt_gc.label_hold), GTK_JUSTIFY_RIGHT);

  /* Row 4 */
  label_fifo = gui_createLabel(app, "FIFO", "label_fifo", 60, BUTTON_HEIGHT);
  frame_fifo_status = gui_createFrame(app, NULL, "frame_fifo_status", 120, 26);

  gtk_table_attach (GTK_TABLE (table_global), label_fifo, 0, 1, 3, 4,
                   (GtkAttachOptions) (0),  (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), frame_fifo_status, 1, 2, 3, 4,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  hbox_pixs_fifo = gui_createHbox(app, "hbox_pixs_fifo", 0, 5);
  gtk_container_add (GTK_CONTAINER (frame_fifo_status), hbox_pixs_fifo);
  gtk_widget_set_usize (hbox_pixs_fifo, 100, BUTTON_HEIGHT);

  svt_gc.pixmap_fifo_status 
    = gui_createIcon(app, "pixmap_fifo_status", svt_gc.pix_file_gray); 
  gtk_box_pack_start(GTK_BOX(hbox_pixs_fifo), svt_gc.pixmap_fifo_status, FALSE, FALSE, 0);

  svt_gc.label_fifo = gui_createLabel(app, "Waiting", "label_fifo", -1, -1);
  gtk_box_pack_end (GTK_BOX (hbox_pixs_fifo), svt_gc.label_fifo, TRUE, FALSE, 0);
  gtk_label_set_justify (GTK_LABEL (svt_gc.label_fifo), GTK_JUSTIFY_RIGHT);

  /* Row 4, Error registers */
  labelx = gui_createLabel(app,"Error Register", "labelx", 100, BUTTON_HEIGHT);
  svt_gc.entry_errreg = 
    gui_createEntryWidget(app, "entry_errreg", 100, BUTTON_HEIGHT, "", TRUE, FALSE);
  gtk_table_attach (GTK_TABLE (table_global), labelx, 2, 3, 3, 4,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), svt_gc.entry_errreg, 3, 4, 3, 4,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  /* Row 5 */
  label16 = gui_createLabel(app, "Address", "label16", 50, BUTTON_HEIGHT);
  svt_gc.entry_address = 
    gui_createEntryWidget(app, "entry_address", 120, BUTTON_HEIGHT, "", TRUE, TRUE);
  svt_gc.entry_result = 
    gui_createEntryWidget(app, "entry_result", 100, BUTTON_HEIGHT, "", TRUE, FALSE);
  hbox11 = gui_createVbox(app, "hbox11", 0, 0);
  hbox13 = gui_createHbox(app, "hbox13", 0, 0);
  button_add_go = gui_createButton(app, "Go", "button_add_go", BUTTON_WIDTH, BUTTON_HEIGHT, 0);

  gtk_table_attach (GTK_TABLE (table_global), label16, 0, 1, 4, 5,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), svt_gc.entry_address, 1, 2, 4, 5,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), svt_gc.entry_result, 2, 3, 4, 5,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), hbox13, 3, 4, 4, 5,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), hbox11, 4, 5, 4, 5,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), button_add_go, 5, 6, 4, 5,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  svt_gc.radio_read_add = 
    gui_createRadioButton(app, address_group, "Read", "radio_read_add", -1, -1, TRUE);
  address_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_read_add));

  svt_gc.radio_write_add = 
    gui_createRadioButton(app, address_group, "Write", "radio_write_add", -1, -1, FALSE);
  address_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_write_add));

  gtk_box_pack_start (GTK_BOX (hbox11), svt_gc.radio_read_add, FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox11), svt_gc.radio_write_add, FALSE, FALSE, 0);

  label17 = gui_createLabel(app, "Times", "label17", 40, BUTTON_HEIGHT);
  gtk_box_pack_start (GTK_BOX (hbox13), label17, FALSE, FALSE, 0);

  svt_gc.entry_times = 
    gui_createEntryWidget(app, "entry_times", 40, BUTTON_HEIGHT, "1", TRUE, TRUE);
  gtk_box_pack_start (GTK_BOX (hbox13), svt_gc.entry_times, FALSE, FALSE, 0);

  /* Row 6 */
  label11 = gui_createLabel(app, "Identity", "label11", 50, BUTTON_HEIGHT);

  hbox_idprom_entry = gui_createHbox(app, "hbox_idprom_entry", 0, 0);
  svt_gc.entry_type = 
    gui_createEntryWidget(app, "entry_type", 60, BUTTON_HEIGHT, "", TRUE, FALSE);
  svt_gc.entry_serial = 
    gui_createEntryWidget(app, "entry_serial", 60, BUTTON_HEIGHT, "", TRUE, FALSE);
  gtk_box_pack_start (GTK_BOX (hbox_idprom_entry), svt_gc.entry_type, FALSE, FALSE, 0);
  gtk_box_pack_end (GTK_BOX (hbox_idprom_entry), svt_gc.entry_serial, FALSE, FALSE, 0);

  hbox14 = gui_createHbox(app, "hbox14", 10, 0);
  svt_gc.pixmap_idprom = gui_createIcon(app, "pixmap_idprom", svt_gc.pix_file_gray); 
  svt_gc.label_idprom_check = 
    gui_createLabel(app, "No Information", "label_idprom_check", 90, BUTTON_HEIGHT);
  gtk_box_pack_start (GTK_BOX (hbox14), svt_gc.pixmap_idprom, FALSE, FALSE, 0);
  gtk_box_pack_end (GTK_BOX (hbox14), svt_gc.label_idprom_check, FALSE, FALSE, 0);

  gtk_table_attach (GTK_TABLE (table_global), label11, 0, 1, 5, 6,
                   (GtkAttachOptions) (0),  (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), hbox_idprom_entry, 1, 2, 5, 6,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_global), hbox14, 4, 5, 5, 6,
                   (GtkAttachOptions) (GTK_FILL), (GtkAttachOptions) (GTK_FILL), 0, 0);

  /* Row 7 */
  label12 = gui_createLabel(app, "Time since last refresh:", "label12", 150, BUTTON_HEIGHT);
  gtk_table_attach (GTK_TABLE (table_global), label12, 1, 2, 7, 8,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  svt_gc.clock = gtk_clock_new (GTK_CLOCK_INCREASING);
  gtk_widget_set_name (svt_gc.clock, "clock");
  gtk_widget_ref (svt_gc.clock);
  gtk_object_set_data_full (GTK_OBJECT (app), "clock", 
			    svt_gc.clock,(GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (svt_gc.clock);
  gtk_table_attach (GTK_TABLE (table_global), svt_gc.clock, 2, 3, 7, 8,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_clock_set_update_interval (GTK_CLOCK (svt_gc.clock), 1);
  gtk_clock_start(GTK_CLOCK(svt_gc.clock));
  
  /* Label of the tab */
  label_tab_global =  gui_createLabel(app, "Global", "label_tab_global", 60, 15);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[0]), 
       gtk_notebook_get_nth_page(GTK_NOTEBOOK (notebook_widget[0]), 0), label_tab_global);

  /* 
   * Page Memory
   */
  svt_gc.table_mem = gui_createTable(app, "table_mem", 4, 4, -1, -1, 25, 10, 10);
  gtk_container_add (GTK_CONTAINER (notebook_widget[0]), svt_gc.table_mem);

  /* Row 1 */
  svt_gc.combo_mem = gui_createCombo(app, "combo_mem", 160, BUTTON_HEIGHT, 0, 0, null_array);

  hbox8   = gui_createHbox(app, "hbox8", 2, 0);
  label15 = gui_createLabel(app, "Times ", "label15", -1, -1);
  svt_gc.entry_mem_times = gui_createEntryWidget(app, "entry_mem_times", 50, 
                                                 BUTTON_HEIGHT, "1", TRUE, TRUE);

  gtk_box_pack_start (GTK_BOX (hbox8), label15, FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox8), svt_gc.entry_mem_times, FALSE, FALSE, 0);

  frame_mem_pix = gui_createFrame(app, NULL, "frame_mem_pix", 200, BUTTON_HEIGHT);

  hbox10 = gui_createHbox(app, "hbox10", 30, 5);
  gtk_container_add (GTK_CONTAINER (frame_mem_pix), hbox10);

  svt_gc.pixmap_memtest = gui_createIcon(app, "pixmap_memtest", svt_gc.pix_file_green); 
  svt_gc.label_memtest = gui_createLabel(app, "0 Error", "label_memtest", -1, -1); 

  gtk_box_pack_start (GTK_BOX (hbox10), svt_gc.pixmap_memtest, FALSE, FALSE, 0);
  gtk_box_pack_end (GTK_BOX (hbox10), svt_gc.label_memtest, FALSE, FALSE, 0);

  button_mem_test = gui_createButton(app, "Test", "button_mem_test", BUTTON_WIDTH, BUTTON_HEIGHT, 0);

  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), svt_gc.combo_mem, 0, 1, 0, 1,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), hbox8, 1, 2, 0, 1,
                   (GtkAttachOptions) (GTK_EXPAND), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), frame_mem_pix, 2, 3, 0, 1,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), button_mem_test, 3, 4, 0, 1,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  /* Row 2 */
  svt_gc.radio_mem_read = gui_createRadioButton(app, read_mem_group, "Read", 
        "radio_mem_read", 60, BUTTON_HEIGHT, TRUE);
  read_mem_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_mem_read));

  svt_gc.radio_mem_write = gui_createRadioButton(app, read_mem_group, "Write from", 
        "radio_mem_write", 100, BUTTON_HEIGHT, FALSE);
  read_mem_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_mem_write));

  svt_gc.radio_mem_compare = gui_createRadioButton(app, read_mem_group, "Compare to", 
             "radio_mem_compare", 100, BUTTON_HEIGHT, FALSE);
  read_mem_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_mem_compare));
  gtk_widget_set_sensitive(svt_gc.radio_mem_compare, FALSE);

  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), svt_gc.radio_mem_read, 0, 1, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), svt_gc.radio_mem_write, 1, 2, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), svt_gc.radio_mem_compare, 2, 3, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  /* Row 3 */
  hbox_display_mem = gui_createHbox(app, "hbox_display_mem", 30, 0);
  svt_gc.radio_mem_display = gui_createRadioButton(app, mem_display_group, "Display", 
      "radio_mem_display", 60, BUTTON_HEIGHT, TRUE);
  mem_display_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_mem_display));

  svt_gc.radio_mem_dump = gui_createRadioButton(app, mem_display_group, "File", 
      "radio_mem_dump", 60, BUTTON_HEIGHT, FALSE);
  mem_display_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_mem_dump));
  gtk_box_pack_start (GTK_BOX (hbox_display_mem), svt_gc.radio_mem_display, FALSE, FALSE, 0);
  gtk_box_pack_end (GTK_BOX (hbox_display_mem), svt_gc.radio_mem_dump, FALSE, FALSE, 0);

  svt_gc.entry_file = gui_createFileEntry(); 
  gtk_widget_set_usize (svt_gc.entry_file, 400, BUTTON_HEIGHT);
  gtk_widget_set_sensitive(svt_gc.entry_file, FALSE);

  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), hbox_display_mem, 0, 1, 2, 3,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), svt_gc.entry_file, 1, 4, 2, 3,
                   (GtkAttachOptions) (0),
                   (GtkAttachOptions) (0), 0, 0);

  /* Row 4 */
  svt_gc.radio_mem_all = gui_createRadioButton(app, num_word_group, "All", 
        "radio_mem_all", 60, BUTTON_HEIGHT, FALSE);
  num_word_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_mem_all));

  hbox15 = gui_createHbox(app, "hbox15", 0, 0);

  svt_gc.radio_mem_num_word = gui_createRadioButton(app, num_word_group, "Words: ", 
            "radio_mem_num_word", 60, BUTTON_HEIGHT, TRUE);
  num_word_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_mem_num_word));

  svt_gc.entry_mem_nw = 
    gui_createEntryWidget(app, "entry_mem_nw", 50, BUTTON_HEIGHT, "100", TRUE, TRUE);

  gtk_box_pack_start (GTK_BOX (hbox15), svt_gc.radio_mem_num_word, FALSE, FALSE, 0);
  gtk_box_pack_end (GTK_BOX (hbox15), svt_gc.entry_mem_nw, FALSE, FALSE, 0);

  hbox6 = gui_createHbox(app, "hbox6", 10, 0);
  button_mem_go = gui_createButton(app, "Go", "button_mem_go", BUTTON_WIDTH, BUTTON_HEIGHT, 0);

  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), hbox15, 1, 2, 3, 4,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), svt_gc.radio_mem_all , 0, 1, 3, 4,
                   (GtkAttachOptions) (0),(GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), hbox6, 2, 3, 3, 4,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_mem), button_mem_go, 3, 4, 3, 4,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  label13 =  gui_createLabel(app, "Offset", "label13", 40, BUTTON_HEIGHT);
  svt_gc.entry_offset = 
    gui_createEntryWidget(app, "entry_offset", 60, BUTTON_HEIGHT, "0", TRUE, TRUE);

  gtk_box_pack_start (GTK_BOX (hbox6), label13, FALSE, FALSE, 0);
  gtk_box_pack_end (GTK_BOX (hbox6), svt_gc.entry_offset, FALSE, FALSE, 0);

  /* Tab label */
  label_tab_memory = gui_createLabel(app, "Memory", "label_tab_memory", 60, 15);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[0]), 
    gtk_notebook_get_nth_page (GTK_NOTEBOOK (notebook_widget[0]), 1), label_tab_memory);

  /* 
   * Page Spy Buffer
   */
  svt_gc.table_spy = gui_createTable(app, "table_spy", 5, 5, -2, -2, 5, 5, 10);
  gtk_container_add (GTK_CONTAINER (notebook_widget[0]), svt_gc.table_spy);

  /* Row 1 */
  svt_gc.label_spy_buffer = 
                     gui_createLabel(app, "Spy Buffer", "label_spy_buffer", 90, BUTTON_HEIGHT);
  svt_gc.combo_spy = gui_createCombo(app, "combo_spy", 90, BUTTON_HEIGHT, 0, 0, null_array);

  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.label_spy_buffer, 0, 1, 0, 1,
                              (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.combo_spy, 3, 5, 0, 1,
                              (GtkAttachOptions) (GTK_EXPAND | GTK_FILL),
                              (GtkAttachOptions) (0), 0, 0);

  /* Row 2 */
  label_spy_pointer = gui_createLabel(app, "Pointer", "label_spy_pointer", 70, BUTTON_HEIGHT);
  gtk_label_set_justify (GTK_LABEL (label_spy_pointer), GTK_JUSTIFY_RIGHT);

  svt_gc.entry_pointer =  
      gui_createEntryWidget(app, "entry_pointer", 60, BUTTON_HEIGHT, "", TRUE, TRUE);

  vbox1 = gui_createVbox(app, "vbox1", 5, 0);
  hbox_wrap = gui_createHbox(app, "hbox_wrap", 0, 0);
  gtk_box_pack_start (GTK_BOX (vbox1), hbox_wrap, FALSE, FALSE, 0);

  label_spy_wrap = gui_createLabel(app, "Wrap: ", "label_spy_wrap", 90, BUTTON_HEIGHT);
  gtk_box_pack_start (GTK_BOX (hbox_wrap), label_spy_wrap, FALSE, FALSE, 0);

  svt_gc.pixmap_spy_wrap = 
     gui_createIcon(app, "pixmap_spy_wrap", svt_gc.pix_file_green); 
  gtk_box_pack_start (GTK_BOX (hbox_wrap), svt_gc.pixmap_spy_wrap, FALSE, FALSE, 0);

  hbox_freeze = gui_createHbox (app, "hbox_freeze", 0, 0);
  gtk_box_pack_start (GTK_BOX (vbox1), hbox_freeze, FALSE, FALSE, 0);

  label_spy_freeze = gui_createLabel(app, "Freeze: ", "label_spy_freeze", 90, BUTTON_HEIGHT);
  gtk_box_pack_start (GTK_BOX (hbox_freeze), label_spy_freeze, FALSE, FALSE, 0);

  svt_gc.pixmap_spy_freeze = 
     gui_createIcon(app, "pixmap_spy_freeze", svt_gc.pix_file_green); 
  gtk_box_pack_start (GTK_BOX (hbox_freeze), svt_gc.pixmap_spy_freeze, FALSE, FALSE, 0); 

  button_spy_reset = 
    gui_createButton(app, "Reset", "button_spy_reset", BUTTON_WIDTH, BUTTON_HEIGHT, 0);

  button_spy_status_read = 
    gui_createButton(app, "Read", "button_spy_status_read", BUTTON_WIDTH, BUTTON_HEIGHT, 0);

  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), label_spy_pointer, 0, 1, 1, 2,
                    (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.entry_pointer, 1, 2, 1, 2,
                    (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), vbox1, 2, 3, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), button_spy_reset, 3, 4, 1, 2,
                    (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), button_spy_status_read, 4, 5, 1, 2,
                    (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  /* Row 3 */
  svt_gc.radio_spy_display = 
    gui_createRadioButton(app, file_group, "Display", "radio_spy_display", 70, 35, TRUE);
  file_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_spy_display));
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.radio_spy_display, 0, 1, 2, 3,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  svt_gc.radio_spy_dump = 
    gui_createRadioButton(app, file_group, "Dump", "radio_spy_dump", 65, 35, FALSE);
  file_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_spy_dump));
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.radio_spy_dump, 1, 2, 2, 3,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  svt_gc.text_spy_file = gui_createFileEntry(); 
  gtk_widget_set_usize (svt_gc.text_spy_file, 320, BUTTON_HEIGHT);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.text_spy_file, 2, 4, 2, 3,
                   (GtkAttachOptions) (GTK_EXPAND),  (GtkAttachOptions) (0), 0, 0);
  gtk_widget_set_sensitive(svt_gc.text_spy_file, FALSE);

  svt_gc.ee_check_button = 
    gui_createCheckButton(app, "EE Only", "ee_check_button", 65, 35, FALSE);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.ee_check_button, 4, 5, 2, 3,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  /* Row 4 */
  label_spy_num_words = gui_createLabel(app, "Words", "label_spy_num_words", 70, BUTTON_HEIGHT);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), label_spy_num_words, 0, 1, 3, 4,
                    (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_label_set_justify (GTK_LABEL (label_spy_num_words), GTK_JUSTIFY_LEFT);

  svt_gc.text_spy_num_word = 
    gui_createEntryWidget(app, "text_spy_num_word", 60, BUTTON_HEIGHT, "10", TRUE, TRUE);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.text_spy_num_word, 1, 2, 3, 4,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  svt_gc.radio_spy_tail = 
    gui_createRadioButton(app, num_w_group, "Tail", "radio_spy_tail", 75, 35, TRUE);
  num_w_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_spy_tail));
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.radio_spy_tail, 2, 3, 3, 4,
                    (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  svt_gc.radio_spy_all = 
    gui_createRadioButton(app, num_w_group, "All", "radio_spy_all", 75, 35, FALSE);
  num_w_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_spy_all));
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), svt_gc.radio_spy_all, 3, 4, 3, 4,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  button_spy_read =  gui_createButton(app, "Read", "button_spy_read", BUTTON_WIDTH, BUTTON_HEIGHT, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), button_spy_read, 4, 5, 3, 4,
                    (GtkAttachOptions) (GTK_EXPAND),(GtkAttachOptions) (0), 0, 0);

  /* Row 5 */

  sc_freeze_label = gui_createLabel(app, "Freeze", "sc_freeze_label", 70, 15);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), sc_freeze_label, 0, 1, 4, 5,
                    (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  sc_freeze_entry = 
    gui_createEntryWidget(app, "sc_freeze_entry", 60, BUTTON_HEIGHT, "", TRUE, FALSE);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), sc_freeze_entry, 1, 2, 4, 5,
                    (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  sc_freeze_button =  gui_createButton(app, "Send", "sc_freeze_button", BUTTON_WIDTH, BUTTON_HEIGHT, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), sc_freeze_button, 2, 3, 4, 5,
                    (GtkAttachOptions) (GTK_EXPAND),(GtkAttachOptions) (0), 0, 0);

  sc_release_button =  gui_createButton(app, "Release", "sc_release_button", BUTTON_WIDTH, BUTTON_HEIGHT, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_spy), sc_release_button, 3, 4, 4, 5,
                    (GtkAttachOptions) (GTK_EXPAND),(GtkAttachOptions) (0), 0, 0);

  /* Now the label of the tab */
  label_tab_spy_buffer = gui_createLabel(app, "Spy Buffer", "label_tab_spy_buffer", 60, 15);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[0]), 
    gtk_notebook_get_nth_page (GTK_NOTEBOOK (notebook_widget[0]), 2), label_tab_spy_buffer);

  /* Page Registers
   * Create a table and place components
   * 'combo_box' | 'entry_box' | 'read_button'   |   'write_button'      <-- 5 rows
   *                           'read_all_button' | 'write_all_button'
   */
  svt_gc.table_regs = gui_createTable(app, "table_regs", 6, 4, -2, -2, 10, 10, 10);
  gtk_container_add (GTK_CONTAINER (notebook_widget[0]), svt_gc.table_regs);

  /* Row 1 ... 5 */
  for (i = 0; i < NUM_REGS; i++) {
    g_snprintf(combo_reg_str, NEL(combo_reg_str), "combo_reg[%d]", i);
    g_snprintf(hbox_reg_str,  NEL(hbox_reg_str),  "reg_le_hbox[%d]", i);
    g_snprintf(label_reg_str, NEL(label_reg_str), "label_reg_x[%d]", i);
    g_snprintf(entry_reg_str, NEL(entry_reg_str), "entry_reg[%d]", i);
    g_snprintf(read_reg_str,  NEL(read_reg_str),  "button_read_reg[%d]", i);
    g_snprintf(write_reg_str, NEL(write_reg_str), "button_write_reg[%d]", i);

    svt_gc.combo_reg[i]
        = gui_createCombo(app, combo_reg_str, 200, BUTTON_HEIGHT, 0, 0, null_array);
    reg_le_hbox[i] = gui_createHbox(app, hbox_reg_str, 1, 1);
    label_reg_x[i] =  gui_createLabel(app, "0x", label_reg_str, 15, BUTTON_HEIGHT);
    svt_gc.entry_reg[i]
        = gui_createEntryWidget(app, entry_reg_str, 80, BUTTON_HEIGHT, "", TRUE, TRUE);
    button_read_reg[i]
        = gui_createButton(app, "Read", read_reg_str, BUTTON_WIDTH, BUTTON_HEIGHT, 0);
    button_write_reg[i] 
        = gui_createButton(app, "Write", write_reg_str, BUTTON_WIDTH,  BUTTON_HEIGHT, 0);

    gtk_box_pack_start(GTK_BOX (reg_le_hbox[i]), label_reg_x[i], FALSE, FALSE, 0);
    gtk_box_pack_end  (GTK_BOX (reg_le_hbox[i]), svt_gc.entry_reg[i], FALSE, FALSE, 0);

    gtk_table_attach (GTK_TABLE (svt_gc.table_regs), svt_gc.combo_reg[i], 0, 1, i, i+1,
                     (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
    gtk_table_attach (GTK_TABLE (svt_gc.table_regs), reg_le_hbox[i], 1, 2, i, i+1,
                     (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
    gtk_table_attach (GTK_TABLE (svt_gc.table_regs), button_read_reg[i], 2, 3, i, i+1,
                     (GtkAttachOptions) (GTK_EXPAND), (GtkAttachOptions) (0), 0, 0);
    gtk_table_attach (GTK_TABLE (svt_gc.table_regs), button_write_reg[i], 3, 4, i, i+1,
                     (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  }
  /* Row 6 */
  button_clear_reg_all 
      = gui_createButton(app, "ClearAll", "button_clear_reg_all", BUTTON_WIDTH, BUTTON_HEIGHT, 0);
  button_read_reg_all 
      = gui_createButton(app, "ReadAll", "button_read_reg_all", BUTTON_WIDTH, BUTTON_HEIGHT, 0);
  button_write_reg_all 
      = gui_createButton(app, "WriteAll", "button_write_reg_all", BUTTON_WIDTH, BUTTON_HEIGHT, 0);
  button_test_reg_all 
      = gui_createButton(app, "TestAll", "button_test_reg_all", BUTTON_WIDTH, BUTTON_HEIGHT, 0);

  gtk_table_attach (GTK_TABLE (svt_gc.table_regs), button_clear_reg_all, 0, 1, 5, 6,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_regs), button_test_reg_all, 1, 2, 5, 6,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_regs), button_read_reg_all, 2, 3, 5, 6,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (svt_gc.table_regs), button_write_reg_all, 3, 4, 5, 6,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  /* Now the tab label */
  label1 = gui_createLabel(app, "Registers", "label1", 50, 15);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[0]), 
      gtk_notebook_get_nth_page (GTK_NOTEBOOK (notebook_widget[0]), 3), label1);

  /* 
   * Page FIFO 
   */
  svt_gc.table_fifo_1 = gui_createTable(app, "table_fifo_1", 4, 1, -2, -2, 5, 2, 3);
  gtk_container_add (GTK_CONTAINER (notebook_widget[0]), svt_gc.table_fifo_1);

  /* Row 1 */
  hbox_fifo_1 = gui_createHbox(app, "hbox_fifo_1", 3, 1);
  gtk_table_attach (GTK_TABLE (svt_gc.table_fifo_1), hbox_fifo_1, 0, 1, 0, 1,
                   (GtkAttachOptions) (GTK_EXPAND | GTK_FILL), 
                   (GtkAttachOptions) (0), 0, 0);
 
  label_fifo1 = gui_createLabel(app, "Status ", "label_fifo1", 60, BUTTON_HEIGHT);
  gtk_box_pack_start (GTK_BOX (hbox_fifo_1), label_fifo1, FALSE, FALSE, 10);

  for (i = 0; i < MAX_FIFO; i++) {
    g_snprintf(name, NEL(name), "%s[%d]", "pixmap_fifo", i);
    svt_gc.pixmap_fifo[i] =  gui_createIcon(app, name, svt_gc.pix_file_gray); 
    gtk_box_pack_start (GTK_BOX (hbox_fifo_1), svt_gc.pixmap_fifo[i], FALSE, FALSE, 10);
  }
  button_fifo_status = gui_createButton(app, "Read", "button_fifo_status", 
                                        BUTTON_WIDTH,  BUTTON_HEIGHT, 0);
  gtk_box_pack_end (GTK_BOX (hbox_fifo_1), button_fifo_status, FALSE, FALSE, 10);

  /* Row 2 */
  hbox_fifo_2 = gui_createHbox(app, "hbox_fifo_2", 3, 1);
  gtk_table_attach (GTK_TABLE (svt_gc.table_fifo_1), hbox_fifo_2, 0, 1, 1, 2,
                   (GtkAttachOptions) (GTK_EXPAND | GTK_FILL), 
                   (GtkAttachOptions) (0), 0, 0);
 
  label_fifo2 = gui_createLabel(app, "Name ", "label_fifo2", 60, BUTTON_HEIGHT);
  gtk_box_pack_start (GTK_BOX (hbox_fifo_2), label_fifo2, FALSE, FALSE, 10);

  for (i = 0; i < MAX_FIFO; i++) {
    g_snprintf(tag, NEL(tag), "%d", i);
    g_snprintf(name, NEL(name), "%s[%d]", "name_fifo", i);
    svt_gc.name_fifo[i] = gui_createLabel(app, tag, name, 10, BUTTON_HEIGHT); 
    gtk_box_pack_start (GTK_BOX (hbox_fifo_2), svt_gc.name_fifo[i], FALSE, FALSE, 10);
  }
  /* Row 3 */
  read_fifo_frame = gui_createFrame(app, " Read FIFO ", "read_fifo_frame", 550, -1);
  gtk_table_attach (GTK_TABLE (svt_gc.table_fifo_1), read_fifo_frame, 0, 1, 2, 3,
                   (GtkAttachOptions) (GTK_EXPAND | GTK_FILL), (GtkAttachOptions) (0), 0, 0);

  /* Within the frame create another table */
  table_fifo_2 = gui_createTable(app, "table_fifo_2", 3, 4, -2, -2, 5, 5, 5);
  gtk_container_add (GTK_CONTAINER (read_fifo_frame), table_fifo_2);

  /* First Row */
  label_fifo8       = gui_createLabel(app, "FIFO", "label_fifo8", 90, BUTTON_HEIGHT);
  svt_gc.combo_fifo = gui_createCombo(app, "combo_fifo", 200, BUTTON_HEIGHT, 0, 0, null_array);
  gtk_table_attach (GTK_TABLE (table_fifo_2), label_fifo8, 0, 1, 0, 1,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_fifo_2), svt_gc.combo_fifo, 3, 4, 0, 1,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  /* Second Row */ 
  svt_gc.radio_display_fifo = gui_createRadioButton(app, fifo_radio_group, "Display", 
                                                    "radio_display_fifo", -1, -1, TRUE);
  fifo_radio_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_display_fifo));

  svt_gc.radio_dump_fifo = gui_createRadioButton (app, fifo_radio_group, "File", 
                                                   "radio_dump_fifo", -1, -1, FALSE);
  fifo_radio_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_dump_fifo));

  svt_gc.entry_fifo_file = gui_createFileEntry(); 
  gtk_widget_set_usize (svt_gc.entry_fifo_file, 300, BUTTON_HEIGHT);
  gtk_widget_set_sensitive(svt_gc.entry_fifo_file, FALSE);

  gtk_table_attach (GTK_TABLE (table_fifo_2), svt_gc.radio_display_fifo, 0, 1, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_fifo_2), svt_gc.radio_dump_fifo, 1, 2, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_fifo_2), svt_gc.entry_fifo_file, 2, 4, 1, 2,
                   (GtkAttachOptions) (GTK_EXPAND | GTK_FILL), (GtkAttachOptions) (0), 0, 0);

  /* Third Row */
  svt_gc.radio_fifo_num_word = gui_createRadioButton(app, num_word_fifo_group, "# Words", 
        "radio_fifo_num_word", -1, -1, TRUE);
  num_word_fifo_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_fifo_num_word));
  gtk_table_attach (GTK_TABLE (table_fifo_2),svt_gc.radio_fifo_num_word, 0, 1, 2, 3,
                   (GtkAttachOptions) (0),(GtkAttachOptions) (0), 0, 0);

  svt_gc.entry_fifo_nw = 
    gui_createEntryWidget(app, "entry_fifo_nw", 40, BUTTON_HEIGHT, "10", TRUE, TRUE);
  gtk_table_attach (GTK_TABLE (table_fifo_2), svt_gc.entry_fifo_nw, 1, 2, 2, 3,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  svt_gc.radio_read_all_fifo = gui_createRadioButton(app, num_word_fifo_group, "All", 
        "radio_read_all_fifo", -1, -1, FALSE);
  num_word_fifo_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_read_all_fifo));
  gtk_table_attach (GTK_TABLE (table_fifo_2), svt_gc.radio_read_all_fifo, 2, 3, 2, 3,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);

  button_fifo_read = gui_createButton(app, "Read", "button_fifo_read", BUTTON_WIDTH, BUTTON_HEIGHT, 0);
  gtk_table_attach (GTK_TABLE (table_fifo_2), button_fifo_read, 3, 4, 2, 3,
                   (GtkAttachOptions) (0),(GtkAttachOptions) (0), 0, 0);

  /* Row 4 */
  /* Frame with legend image */
  legend_fifo_frame = gui_createFrame(app, " Legend ", "legend_fifo_frame", 550, -1);
  gtk_table_attach (GTK_TABLE (svt_gc.table_fifo_1), legend_fifo_frame, 0, 1, 3, 4,
                   (GtkAttachOptions) (GTK_EXPAND | GTK_FILL),
                   (GtkAttachOptions) (0), 0, 0);

  hbox_legend = gui_createHbox(app, "hbox_legend", 5, 5);
  gtk_container_add (GTK_CONTAINER (legend_fifo_frame), hbox_legend);
  hbox_leg_white = gui_createHbox(app, "hbox_leg_white", 5, 5);
  gtk_box_pack_start (GTK_BOX (hbox_legend), hbox_leg_white, FALSE, FALSE, 0);

  gtk_box_pack_start (GTK_BOX (hbox_leg_white), 
    gui_createIcon(app, "white_ball", svt_gc.pix_file_white), FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox_leg_white), 
    gui_createLabel(app, "Empty", "white_label", 60, BUTTON_HEIGHT), TRUE, FALSE, 0);
  hbox_leg_green = gui_createHbox(app, "hbox_leg_green", 5, 5);
  gtk_box_pack_start (GTK_BOX (hbox_legend), hbox_leg_green, FALSE, FALSE, 0);

  gtk_box_pack_start (GTK_BOX (hbox_leg_green), 
    gui_createIcon(app, "green_ball", svt_gc.pix_file_green), FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox_leg_green), 
    gui_createLabel(app, "With data", "green_label", 60, BUTTON_HEIGHT), TRUE, FALSE, 0);

  hbox_leg_yellow = gui_createHbox(app, "hbox_leg_yellow", 5, 1);
  gtk_box_pack_start (GTK_BOX (hbox_legend), hbox_leg_yellow, FALSE, FALSE, 0);

  gtk_box_pack_start (GTK_BOX (hbox_leg_yellow), 
    gui_createIcon(app, "yellow_ball", svt_gc.pix_file_yellow), FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox_leg_yellow), 
    gui_createLabel(app, "Hold", "yellow_label", 60, BUTTON_HEIGHT), TRUE, FALSE, 0);

  hbox_leg_red = gui_createHbox(app, "hbox_leg_red", 5, 1);
  gtk_box_pack_start (GTK_BOX (hbox_legend), hbox_leg_red, FALSE, FALSE, 0);

  gtk_box_pack_start (GTK_BOX (hbox_leg_red), 
    gui_createIcon(app, "red_ball", svt_gc.pix_file_red), FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox_leg_red), 
    gui_createLabel(app, "Overfl", "red_label", 60, BUTTON_HEIGHT), TRUE, FALSE, 0);

  hbox_leg_gray = gui_createHbox(app, "hbox_leg_gray", 5, 1);
  gtk_box_pack_start (GTK_BOX (hbox_legend), hbox_leg_gray, FALSE, FALSE, 0);

  gtk_box_pack_start (GTK_BOX (hbox_leg_gray), 
    gui_createIcon(app, "gray_ball", svt_gc.pix_file_gray), FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox_leg_gray), 
    gui_createLabel(app, "No Information", "gray_label", 100, BUTTON_HEIGHT), TRUE, FALSE, 0);

  /* Now the tab label */
  label2 =  gui_createLabel(app, "FIFO", "label2", 60, 15);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[0]), 
    gtk_notebook_get_nth_page (GTK_NOTEBOOK (notebook_widget[0]), 4), label2);

  /* 
   * Page Send Data 
   */
  table_send_data = gui_createTable(app, "table_send_data", 3, 2, -1, -1, 1, 20, 3);
  gtk_container_add (GTK_CONTAINER (notebook_widget[0]), table_send_data);
  
  /* 
   * Row 1, a frame which contains all the options for sending data
   */
  frame_send_option = gui_createFrame(app, " Send Options ","frame_send_option", 460, 150);
  vbox_send_option = gui_createVbox(app, "vbox_send_option", 0, 1);
  gtk_container_add (GTK_CONTAINER (frame_send_option), vbox_send_option);

  hbox_send_option = gui_createHbox(app, "hbox_send_option", 20, 0);

  svt_gc.radio_send_option[0] = gui_createRadioButton(app, send_data_group, "Send Data Once", 
                                                      "radio_send_option[0]", -1, -1, TRUE);
  send_data_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_send_option[0]));

  send_label_speed        = gui_createLabel(app, "Speed:", "send_label_speed", 40, BUTTON_HEIGHT);
  svt_gc.send_combo_speed = gui_createCombo(app, "send_combo_speed", 110, BUTTON_HEIGHT, 0, 2, speed_opt);
  gtk_box_pack_start (GTK_BOX (hbox_send_option), svt_gc.radio_send_option[0], FALSE, FALSE, 0);
  gtk_box_pack_end   (GTK_BOX (hbox_send_option), svt_gc.send_combo_speed, FALSE, FALSE, 0);
  gtk_box_pack_end   (GTK_BOX (hbox_send_option), send_label_speed, FALSE, FALSE, 0);

  svt_gc.radio_send_option[1] = gui_createRadioButton(app, send_data_group, "Send Data in a Loop", 
                                                      "radio_send_option[1]", -1, -1, FALSE);
  send_data_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_send_option[1]));

  svt_gc.radio_send_option[2] = gui_createRadioButton(app, send_data_group, "Resend Data Once", 
                                                           "radio_send_option[2]", -1, -1, FALSE);
  send_data_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_send_option[2]));

  svt_gc.radio_send_option[3] = gui_createRadioButton(app, send_data_group, "Resend Data in a Loop", 
                                                           "radio_send_option[3]", -1, -1, FALSE);
  send_data_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_send_option[3]));

  svt_gc.radio_send_option[4] = gui_createRadioButton(app, send_data_group, "Resend Data", 
                                                           "radio_send_option[4]", -1, -1, FALSE);
  send_data_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_send_option[4]));

  gtk_box_pack_start (GTK_BOX (vbox_send_option), hbox_send_option, FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (vbox_send_option), svt_gc.radio_send_option[1], FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (vbox_send_option), svt_gc.radio_send_option[2], FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (vbox_send_option), svt_gc.radio_send_option[3], FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (vbox_send_option), svt_gc.radio_send_option[4], FALSE, FALSE, 0);

  /* 
   * Row 2, data source, data could come from the input area or from a file
   */
  frame_data_source = gui_createFrame(app, " Data Source ", "frame_data_source", 460, 75);

  vbox_data_source = gui_createVbox(app, "vbox_data_source", 0, 1);
  gtk_container_add (GTK_CONTAINER (frame_data_source), vbox_data_source);

  svt_gc.radio_data_source[0] = gui_createRadioButton(app, send_source_group, 
      "Input Area", "radio_data_source[0]", -1, -1, TRUE);
  send_source_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_data_source[0]));
  gtk_box_pack_start (GTK_BOX (vbox_data_source), svt_gc.radio_data_source[0], FALSE, FALSE, 0);

  hbox_data_source = gui_createHbox(app, "hbox_data_source", 10, 0);
  gtk_box_pack_start (GTK_BOX (vbox_data_source), hbox_data_source, FALSE, FALSE, 0);

  svt_gc.radio_data_source[1] = gui_createRadioButton(app, send_source_group, 
      "File", "radio_data_source[1]", -1, -1, FALSE);
  send_source_group = gtk_radio_button_group (GTK_RADIO_BUTTON (svt_gc.radio_data_source[1]));

  svt_gc.send_entry_file = gui_createFileEntry(); 
  gtk_widget_set_usize (svt_gc.send_entry_file, 360, BUTTON_HEIGHT);
  gtk_widget_set_sensitive(svt_gc.send_entry_file, FALSE);

  gtk_box_pack_start (GTK_BOX (hbox_data_source), svt_gc.radio_data_source[1], FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox_data_source), svt_gc.send_entry_file, FALSE, FALSE, 0);

  vbox_send_button = gui_createVbox(app, "vbox_send_button", 10, 0);
  hbox_send_button = gui_createHbox(app, "hbox_send_button", 2, 0);
  gtk_box_pack_start (GTK_BOX (vbox_send_button), hbox_send_button, FALSE, FALSE, 0);

  svt_gc.send_nevt_entry  = 
    gui_createEntryWidget(app, "send_nevt_entry", 50, BUTTON_HEIGHT, "10", TRUE, TRUE);
  gtk_box_pack_start (GTK_BOX (hbox_send_button), 
    gui_createLabel(app, "Words", "no_of_words", 50, BUTTON_HEIGHT), FALSE, FALSE, 0);
  gtk_box_pack_start (GTK_BOX (hbox_send_button), svt_gc.send_nevt_entry, FALSE, FALSE, 0);

  button_send_data = gui_createButton(app, "Send", "button_send_data", BUTTON_WIDTH, BUTTON_HEIGHT, 0);
  gtk_box_pack_start (GTK_BOX (vbox_send_button), button_send_data, FALSE, FALSE, 0);

  gtk_table_attach (GTK_TABLE (table_send_data), frame_send_option, 0, 1, 0, 1,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_send_data), frame_data_source, 0, 1, 1, 2,
                   (GtkAttachOptions) (0), (GtkAttachOptions) (0), 0, 0);
  gtk_table_attach (GTK_TABLE (table_send_data), vbox_send_button, 1, 2, 1, 2,
                   (GtkAttachOptions) (GTK_EXPAND), (GtkAttachOptions) (0), 0, 0);

  /* Now the tab label */
  label_tab_send_data =  gui_createLabel(app, "Send Data", "label_tab_send_data", 60, 15);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[0]), 
    gtk_notebook_get_nth_page (GTK_NOTEBOOK (notebook_widget[0]), 5), label_tab_send_data);

  /* 
   * Use another notebook at the bottom to hold 'message area', 'data area' 
   * and the 'scripting' area
   */
  notebook_widget[1] = gui_createNotebook(app, "notebook_widget[1]");
  gtk_box_pack_end (GTK_BOX (top_level_v_box), notebook_widget[1], TRUE, TRUE, 0);

  message_area_frame = gui_createFrame(app, NULL, "message_area_frame", -2, -2);
  gtk_container_set_border_width (GTK_CONTAINER (message_area_frame), 5);
  gtk_container_add (GTK_CONTAINER (notebook_widget[1]), message_area_frame);

  scrolled_window_1 = gui_createScrollWindow(app, "scrolled_window_1", -1, -1);
  gtk_container_add (GTK_CONTAINER (message_area_frame), scrolled_window_1);

  svt_gc.output_area = gui_createTextWidget(app, "output_area", 100, 100, FALSE);
  gtk_container_add (GTK_CONTAINER (scrolled_window_1), svt_gc.output_area);
  gtk_text_insert (GTK_TEXT (svt_gc.output_area), NULL, NULL, NULL, message, strlen(message));

  data_area_frame = gui_createFrame(app, NULL, "data_area_frame", -2, -2);
  gtk_container_set_border_width (GTK_CONTAINER (data_area_frame), 5);
  gtk_container_add (GTK_CONTAINER (notebook_widget[1]), data_area_frame);

  scrolled_window_2 = gui_createScrollWindow(app, "scrolled_window_2", -1, -1);
  gtk_container_add (GTK_CONTAINER (data_area_frame), scrolled_window_2);

  svt_gc.input_area = gui_createTextWidget(app, "input_area", 100, 100, TRUE);
  gtk_container_add (GTK_CONTAINER (scrolled_window_2), svt_gc.input_area);

  vbox6 = gui_createVbox(app, "vbox6", 0, 0);
  gtk_container_add (GTK_CONTAINER (notebook_widget[1]), vbox6);

  scrolled_window_3 = gui_createScrollWindow(app, "scrolled_window_3", -1, -1);
  gtk_box_pack_start (GTK_BOX (vbox6), scrolled_window_3, TRUE, TRUE, 0);

  svt_gc.script_area = gui_createTextWidget(app, "script_area", 100, 100, TRUE);
  gtk_container_add (GTK_CONTAINER (scrolled_window_3), svt_gc.script_area);
  gtk_text_insert (GTK_TEXT (svt_gc.script_area), NULL, NULL, NULL, _("print b.what()"), 14);

  hbox16 = gui_createHbox(app, "hbox16", 0, 5);
  gtk_box_pack_start (GTK_BOX (vbox6), hbox16, FALSE, FALSE, 0);
  gtk_container_set_border_width (GTK_CONTAINER (hbox16), 5);

  button7 = gnome_stock_button(GNOME_STOCK_PIXMAP_EXEC);
  gtk_widget_set_name (button7, "button7");
  gtk_widget_ref (button7);
  gtk_object_set_data_full (GTK_OBJECT (app), "button7", button7,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (button7);
  gtk_box_pack_end (GTK_BOX (hbox16), button7, FALSE, FALSE, 3);
  gtk_widget_set_usize (button7, 90, 30);

  /* 
   * Execution of Python script 
   */
  python_hbox = gui_createHbox(app, "python_hbox", 10, 4);
  gtk_box_pack_end (GTK_BOX (vbox6), python_hbox, FALSE, TRUE, 0);

  python_run_label = gui_createLabel(app, "Script:", "python_run_label", 20, BUTTON_HEIGHT);
  python_fileentry = gui_createFileEntry(); 
  gtk_widget_set_usize (python_fileentry, 360, BUTTON_HEIGHT);
  python_exec_button = gui_createButton(app, "Run", "python_exec_button", 
                       40, BUTTON_HEIGHT, 0);
  python_save_button = gui_createButton(app, "Save", "python_save_button", 
                       40, BUTTON_HEIGHT, 0);
  gtk_box_pack_start (GTK_BOX (python_hbox), python_run_label, TRUE, TRUE, 0);
  gtk_box_pack_start (GTK_BOX (python_hbox), python_fileentry, TRUE, TRUE, 0);
  gtk_box_pack_start (GTK_BOX (python_hbox), python_exec_button, TRUE, TRUE, 0);
  gtk_box_pack_end (GTK_BOX (python_hbox), python_save_button, TRUE, TRUE, 0);

  label_message_tab =  gui_createLabel(app, "Output", "label_message_tab", 60, 16);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[1]), 
       gtk_notebook_get_nth_page (GTK_NOTEBOOK (notebook_widget[1]), 0), label_message_tab);

  label_data_tab = gui_createLabel(app, "Input", "label_data_tab", 60, 16);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[1]), 
       gtk_notebook_get_nth_page (GTK_NOTEBOOK (notebook_widget[1]), 1), label_data_tab);

  label_script_tab =  gui_createLabel(app, "Python", "label_script_tab", 60, 16);
  gtk_notebook_set_tab_label (GTK_NOTEBOOK (notebook_widget[1]), 
       gtk_notebook_get_nth_page (GTK_NOTEBOOK (notebook_widget[1]), 2), label_script_tab);

  /* If auto connect is enabled connect */
#if 0
  if (svt_gc.verbose_mode) svtgui_setAllDefaults(); 
#endif

  /* 
   * Callbacks for register tab 
   */
  gtk_signal_connect (GTK_OBJECT (python_exec_button), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      python_fileentry);
  gtk_signal_connect (GTK_OBJECT (python_save_button), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      python_fileentry);

  for (i = 0; i < NUM_REGS; i++) {
    gtk_signal_connect (GTK_OBJECT (button_read_reg[i]), "clicked",
                        GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                        GINT_TO_POINTER(i));
    gtk_signal_connect (GTK_OBJECT (button_write_reg[i]), "clicked",
                        GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                        GINT_TO_POINTER(i));
  }
  gtk_signal_connect (GTK_OBJECT (button_read_reg_all), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      "Read");
  gtk_signal_connect (GTK_OBJECT (button_write_reg_all), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      "Write");
  gtk_signal_connect (GTK_OBJECT (button_test_reg_all), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      "Test");
  gtk_signal_connect (GTK_OBJECT (button_clear_reg_all), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_clearRegCB),
                      &svt_gc);

  gtk_signal_connect (GTK_OBJECT (button_init), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);

  gtk_signal_connect (GTK_OBJECT (button_fifo_read), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.button_test_mode), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button_add_go), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button_refresh), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button_send_data), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button_mem_test), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_mem_read), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_mem_write), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button_mem_go), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_mem_compare), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_mem_dump), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_mem_display), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_spy_all), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_spy_display), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_spy_tail), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_dump_fifo), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_display_fifo), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button_spy_read), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_spy_dump), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_mem_all), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_read_all_fifo), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_mem_num_word), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_fifo_num_word), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_read_add), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      svt_gc.entry_result);
  gtk_signal_connect (GTK_OBJECT (svt_gc.radio_write_add), "toggled",
                      GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                      svt_gc.entry_result);

  gtk_signal_connect (GTK_OBJECT (button_spy_reset), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button_spy_status_read), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      NULL);
  gtk_signal_connect (GTK_OBJECT (button_fifo_status), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_buttonClickCB),
                      &svt_gc);

  gtk_signal_connect (GTK_OBJECT (sc_freeze_button), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_sendFreezeCB),
                      sc_freeze_entry);

  gtk_signal_connect (GTK_OBJECT (sc_release_button), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_sendFreezeCB),
                      sc_freeze_entry);

  combo_entry_crate = GTK_COMBO (svt_gc.combo_crate)->entry;
  gtk_signal_connect (GTK_OBJECT(combo_entry_crate), "activate",
		      GTK_SIGNAL_FUNC(svtgui_validateCrateCB),
                      GTK_OBJECT(svt_gc.combo_crate));

  combo_entry_board = GTK_COMBO (svt_gc.combo_board)->entry;
  gtk_signal_connect (GTK_OBJECT(combo_entry_board), "activate",
		      GTK_SIGNAL_FUNC(svtgui_validateBoardCB), NULL); 
  
  for (i = 0; i < 5; i++) {
    gtk_signal_connect (GTK_OBJECT (svt_gc.radio_send_option[i]), "toggled",
                        GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                        svt_gc.send_combo_speed);
  }
  for (i = 0; i < 2; i++) {
    gtk_signal_connect (GTK_OBJECT (svt_gc.radio_data_source[i]), "toggled",
                        GTK_SIGNAL_FUNC (svtgui_radioButtonToggleCB),
                        svt_gc.send_entry_file);
  }
  gtk_object_set_data (GTK_OBJECT (app), "tooltips", tooltips);

  gtk_signal_connect (GTK_OBJECT (button7), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_execScriptCB),
                      svt_gc.script_area);

  gtk_signal_connect (GTK_OBJECT (button_crate_map), "clicked",
                      GTK_SIGNAL_FUNC (svtgui_mapCrateCB), NULL);
}
void svtgui_installMenusAndToolbar(GtkWidget* app)
{
  gnome_app_create_toolbar_with_data(GNOME_APP(app), toolbar, app);
  gnome_app_create_menus_with_data(GNOME_APP(app), menubar, app);
  gnome_app_install_menu_hints(GNOME_APP(app), menubar);
}
