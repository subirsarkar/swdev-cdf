#ifndef SVTGUI_GRAPH_GLOBAL_H
#define SVTGUI_GRAPH_GLOBAL_H
/*
 * All the global widgets needed everywhere are declared within a globally
 * visible structure.
 */
enum {
 MAX_FIFO = 11,
 NUM_REGS = 5
};

struct GlobalComponent 
{
  char pix_file_white[100];
  char pix_file_green[100];
  char pix_file_red[100];
  char pix_file_yellow[100];
  char pix_file_gray[100];

  GtkWidget *combo_crate;
  GtkWidget *combo_board;
  GtkWidget *combo_slot;

  GtkWidget *pixmap_init;
  GtkWidget *label_init;

  GtkWidget *button_test_mode;
  GtkWidget *pixmap_tmode;
  GtkWidget *label_tmode;

  GtkWidget *pixmap_hold;
  GtkWidget *label_hold;

  GtkWidget *pixmap_fifo_status;
  GtkWidget *label_fifo;

  GtkWidget *entry_address;
  GtkWidget *entry_result;
  GtkWidget *radio_read_add;
  GtkWidget *radio_write_add;
  GtkWidget *entry_times;

  GtkWidget *entry_type;
  GtkWidget *entry_serial;
  GtkWidget *pixmap_idprom;
  GtkWidget *label_idprom_check;

  GtkWidget *entry_errreg;
  GtkWidget *clock;

  GtkWidget *table_mem;
  GtkWidget *combo_mem;
  GtkWidget *entry_mem_times;
  GtkWidget *pixmap_memtest;
  GtkWidget *label_memtest;

  GtkWidget *radio_mem_read;
  GtkWidget *radio_mem_write;
  GtkWidget *radio_mem_compare;

  GtkWidget *radio_mem_display;
  GtkWidget *radio_mem_dump;
  GtkWidget *entry_file;

  GtkWidget *radio_mem_all;
  GtkWidget *radio_mem_num_word;
  GtkWidget *entry_mem_nw;
  GtkWidget *entry_offset;

  GtkWidget *table_spy;
  GtkWidget *label_spy_buffer;
  GtkWidget *combo_spy;

  GtkWidget *entry_pointer;
  GtkWidget *pixmap_spy_wrap;
  GtkWidget *pixmap_spy_freeze;

  GtkWidget *radio_spy_display;
  GtkWidget *radio_spy_dump;
  GtkWidget *text_spy_file;

  GtkWidget *text_spy_num_word;
  GtkWidget *radio_spy_tail;
  GtkWidget *radio_spy_all;

  GtkWidget *table_regs;
  GtkWidget *combo_reg[NUM_REGS];
  GtkWidget *entry_reg[NUM_REGS];

  GtkWidget *table_fifo_1;
  GtkWidget *pixmap_fifo[MAX_FIFO]; 
  GtkWidget *name_fifo[MAX_FIFO]; 
  GtkWidget *combo_fifo;
  GtkWidget *radio_display_fifo;
  GtkWidget *radio_dump_fifo;
  GtkWidget *entry_fifo_file;

  GtkWidget *radio_fifo_num_word;
  GtkWidget *entry_fifo_nw;
  GtkWidget *radio_read_all_fifo;

  GtkWidget *radio_send_option[5]; 
  GtkWidget *send_combo_speed;
  GtkWidget *radio_data_source[2]; 
  GtkWidget *send_entry_file;
  GtkWidget *send_nevt_entry;

  GtkWidget *output_area;
  GtkWidget *script_area;
  GtkWidget *input_area;

  GtkWidget *statusbar_app;
  GtkWidget *ee_check_button;

  gboolean verbose_mode;
  gboolean owpl_mode;
};
typedef struct GlobalComponent GlobalComponent;


#endif
