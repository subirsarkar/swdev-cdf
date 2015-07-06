/*
 * All the GUI related convenience functions are defined in this file 
 * In addition to that reading/writing data from/to files, reading data
 * from the input area are also placed here.
 */
#include <gnome.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include "svtvme_public.h"
#include "utils.h"

/* Create a Gnome FileEntry  and return the address */
GtkWidget *gui_createFileEntry() {
   GtkWidget *widget = gnome_file_entry_new("fseekid", "File Seeking");
#if 0
   gnome_file_entry_set_title((GnomeFileEntry *) widget, "File Selection Dialog");
   gnome_file_entry_set_modal((GnomeFileEntry *) widget, TRUE);
#endif
   return widget;
}

/* Create a Gnome entry box  and return the address
 * width   - Length of the entry box
 * height  - Height
 * nitems  - Number of items it holds initially
 * items   - The array of item
 */
GtkWidget *gui_createGnomeEntry(gint width, gint height, gint nitems, gchar *items[]) {
  int i;
  GtkWidget *widget = gnome_entry_new(NULL);
  if (width || height) gtk_widget_set_usize (widget, width, height);
  gnome_entry_set_max_saved(GNOME_ENTRY(widget), 20);
   
  if (nitems) {
    for (i = nitems-1; i >= 0; i--) {
      gnome_entry_append_history(GNOME_ENTRY(widget), TRUE, items[i]);
    }
    gtk_entry_set_text(GTK_ENTRY(gnome_entry_gtk_entry(GNOME_ENTRY(widget))), items[0]);
  }
  return widget;
}

/* Create a vertical box (container)  and return the address
 * app         - The parent application which uses the box
 * name        - Name of the box which can be used to retrive the address of the box
 * col_spacing - Separation between adjacent components 
 * border      - border around the box
 */
GtkWidget *gui_createVbox(GtkWidget *app, gchar *name, gint col_spacing, gint border) {
  GtkWidget *widget = gtk_vbox_new (FALSE, col_spacing);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  gtk_container_set_border_width (GTK_CONTAINER (widget), border);

  return widget;
}

/* Create a horizontal box (container)  and return the address
 * app         - The parent application which uses the box
 * name        - Name of the box which can be used to retrive the address of the box
 * col_spacing - Separation between adjacent components 
 * border      - border around the box
 */
GtkWidget *gui_createHbox(GtkWidget *app, gchar *name, gint row_spacing, gint border) {
  GtkWidget *widget = gtk_hbox_new (FALSE, row_spacing);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  gtk_container_set_border_width (GTK_CONTAINER (widget), border);

  return widget;
}

/* Create a combobox [a GtkEntry and a GtkList]  and return the address
 * app    - The parent application which uses the box
 * name   - Name of the box which can be used to retrive the address of the box
 * width  - Length of the entry box
 * height - Height of the entry box
 * border - border around the combo box
 * nitems - Number of items it holds initially
 * items  - The array of item
 */
GtkWidget *gui_createCombo(GtkWidget *app, gchar *name, 
           gint width, gint height, gint border, gint nitems, gchar *items[]) 
{
  GtkWidget *widget_entry;
  GList *item_list = NULL;
  gint i;

  GtkWidget *widget = gtk_combo_new ();
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                            (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);
  gtk_container_set_border_width (GTK_CONTAINER (widget), border);

  if (nitems) {
    for (i = 0; i < nitems; i++) {
      item_list = g_list_append (item_list, items[i]);
    }
    gtk_combo_set_popdown_strings (GTK_COMBO (widget), item_list);
    g_list_free (item_list);
  }
  /* Combo entry */
  widget_entry = GTK_COMBO (widget)->entry;
  gtk_entry_set_text (GTK_ENTRY (widget_entry), items[0]);

  return widget;
}

/* Create a Gtk Button with a label  and return the address
 * app    - The parent application which uses the box
 * label  - The label which is displayed on the button
 * name   - Name of the button which can be used to retrive the address of the box
 * width  - Length of the button
 * height - Height of the button
 * border - amount of border space around the button
 */
GtkWidget *gui_createButton(GtkWidget *app, gchar *label, gchar *name, 
          gint width, gint height, gint border)
{
  GtkWidget *widget = gtk_button_new_with_label (label);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);
  gtk_container_set_border_width (GTK_CONTAINER (widget), border);

  return widget;
}

/* Create a Gtk NoteBook widget and return the address
 * app    - The parent application which uses the box
 * name   - Name of the NoteBook widget
 */
GtkWidget *gui_createNotebook(GtkWidget *app, gchar *name) {
  GtkWidget *widget = gtk_notebook_new ();
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  gtk_notebook_set_show_border (GTK_NOTEBOOK (widget), FALSE); 

  return widget;
}

/* Create a Gtk Label and return the address
 * app    - The parent application which uses the box
 * tag    - The string which is displayed on the label, i.e label text
 * name   - Name of the button which can be used to retrive the address of the box
 * width  - Length of the button
 * height - Height of the button
 */
GtkWidget *gui_createLabel(GtkWidget *app, gchar *tag, gchar *name, 
          gint width, gint height)
{
  GtkWidget *widget = gtk_label_new (tag);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);

  return widget;
}

/* Create a Gtk Table widget and return the address
 * app    - The parent application which uses the box
 * name   - Name of the button which can be used to retrive the address of the box
 * row    - Number of rows of the table
 * column - Number of columns of the table
 * width  - Length of the button
 * height - Height of the button
 * col_spacing - Space between two adjacent columns 
 * border - amount of border space around the table
 */
GtkWidget *gui_createTable(GtkWidget *app, gchar *name, gint row, gint column, 
           gint width, gint height, gint row_spacing, gint col_spacing, gint border)
{
  GtkWidget *widget = gtk_table_new (row, column, FALSE);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);
  gtk_table_set_row_spacings (GTK_TABLE (widget), row_spacing);
  gtk_table_set_col_spacings (GTK_TABLE (widget), col_spacing);
  gtk_container_set_border_width (GTK_CONTAINER (widget), border);

  return widget;
}

/* Create a Gtk Frame and return the address
 * app    - The parent application which uses the box
 * label  - The label which is displayed on the border of the frame
 * name   - Name of the button which can be used to retrive the address of the box
 * width  - Length of the button
 * height - Height of the button
 */
GtkWidget *gui_createFrame(GtkWidget *app, gchar *label, gchar *name, 
                                           gint width, gint height)
{
  GtkWidget *widget = gtk_frame_new (label);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);

  return widget;
}

/* Create a Gnome icon object and return the address
 * app    - The parent application which uses the box
 * name   - Name of the Icon object which can be used to retrive the address
 * pix_file - xpm file name which is used to create the Icon object
 */
GtkWidget *gui_createIcon(GtkWidget *app, gchar *name, gchar *pix_file) {
  GtkWidget *widget = gnome_pixmap_new_from_file(pix_file); 
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);

  return widget;
}

/* Create a Gtk Text widget and return the address
 * app    - The parent application which uses the Text widget
 * name   - Name of the widget which can be used to retrive the address subsequently
 * width  - Length of the text widget
 * height - Height of the text widget
 * editable - Decide to render the text widget editable or not
 */
GtkWidget *gui_createTextWidget(GtkWidget *app, gchar *name, gint width, 
                                                gint height, gboolean editable)
{
  GtkWidget *widget = gtk_text_new (NULL, NULL);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);
  gtk_text_set_editable (GTK_TEXT (widget), editable);

  return widget;
}

/* Create a Gtk one line Text entry widget and return the address
 * app    - The parent application which uses the Text entry widget
 * name   - Name of the widget which can be used to retrive the address subsequently
 * width  - Length of the text entry widget
 * height - Height of the text entry widget
 * text   - Initial text
 * sensitive - Decide whether accepts or not input initially 
 * editable - Decide to render the text entry widget editable or not
 */
GtkWidget *gui_createEntryWidget(GtkWidget *app, gchar *name, gint width, gint height,
                                        gchar *text, gboolean sensitive, gboolean editable)
{
  GtkWidget *widget = gtk_entry_new ();
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);

  gtk_entry_set_text (GTK_ENTRY (widget), text);
  gtk_widget_set_sensitive(widget, sensitive);
  gtk_entry_set_editable (GTK_ENTRY (widget), editable);

  return widget;
}

/* Create a Gtk Radio Button widget and return the address
 * app    - The parent application which uses the Text widget
 * group  - A GSList which groups a number of RadioButton
 *          such that only one is checked at a time
 * label  - Label which is displayed on the button
 * name   - Name of the widget which can be used to retrive the address subsequently
 * width  - Length of the radio button
 * height - Height of the radio button
 * active - Initially checked or not
 */
GtkWidget *gui_createRadioButton(GtkWidget *app, GSList *group, gchar *label, gchar *name, 
                                     gint width, gint height, gboolean active)
{
  GtkWidget *widget = gtk_radio_button_new_with_label (group, label);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);
  gtk_toggle_button_set_active (GTK_TOGGLE_BUTTON (widget), active);
  
  return widget;
}

/* Create a Gtk Check Button widget and return the address
 * app    - The parent application which uses the Text widget
 * label  - Label which is displayed on the button
 * name   - Name of the widget which can be used to retrive the address subsequently
 * width  - Length of the radio button
 * height - Height of the radio button
 * active - Initially checked or not
 */
GtkWidget *gui_createCheckButton(GtkWidget *app, gchar *label, gchar *name, 
                                                 gint width, gint height, gboolean active)
{
  GtkWidget *widget = gtk_check_button_new_with_label(label);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget,
                           (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);
  gtk_toggle_button_set_active (GTK_TOGGLE_BUTTON (widget), active);
  
  return widget;
}

/* Create a Gtk Scrolled window widget and return the address
 * app    - The parent application which uses the Text widget
 * name   - Name of the widget which can be used to retrive the address subsequently
 * width  - Length of the window
 * height - Height of the window
 */
GtkWidget *gui_createScrollWindow(GtkWidget *app, gchar *name, gint width, gint height) {
  GtkWidget *widget = gtk_scrolled_window_new (NULL, NULL);
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, widget, (GtkDestroyNotify) gtk_widget_unref);
  gtk_widget_show (widget);
  if (width || height) gtk_widget_set_usize (widget, width, height);
  gtk_scrolled_window_set_policy (GTK_SCROLLED_WINDOW (widget), 
             GTK_POLICY_AUTOMATIC, GTK_POLICY_AUTOMATIC);

  return widget;
}

/* Create a Gtk Statusbar widget and return the address
 * app    - The parent application which uses the Text widget
 * name   - Name of the widget which can be used to retrive the address subsequently
 */
GtkWidget *gui_createStatusbar(GtkWidget *app, gchar *name) {
  GtkWidget *widget = gtk_statusbar_new();
  gtk_widget_set_name (widget, name);
  gtk_widget_ref (widget);
  gtk_object_set_data_full (GTK_OBJECT (app), name, 
                            widget, (GtkDestroyNotify) gtk_widget_unref);

  return widget;
}

/* Load data onto an array from a file. The file should contain data words,
 * one word a line
 * filename  - Name of the input file which contains data
 * ndata     - Size of the data array
 * data      - Pointer to the data array
 */
gint tool_loadFromFile(gchar *filename, gint ndata, uint4 *data) {
  gint debug_mode = 0;
  gchar str_data_low[80] = "/";
  gint  index = 0;
  gint  i = 0;
  FILE *fp;

  gchar *str_data = g_new0(gchar, 80);
  if ((fp = fopen(filename, "r")) == NULL) {
    fprintf(stderr, "%s %s\n", "problems reading", filename);
    fclose(fp);
    g_free(str_data);
    return -1;
  }
  while (fgets(str_data, 80, fp) != NULL) {
    i = 0;
    if (debug_mode)
      g_message("str_data = %10s, possible index = %d", str_data, index);
    
    /* check if string is a comment or if it is empty */
    tool_stripSpace(&str_data);
    
    if ( (str_data[0] != '/') && (str_data[0] != 10) && !tool_checkSpace(str_data) ) {
      while (str_data[i] != '\0') {
        str_data_low[i] = tolower(str_data[i]);
        i++;
      }
      str_data_low[i] = '\0';
      if (debug_mode)
        g_message("i = %d, str_data_low = %s", i, str_data_low);
      sscanf(str_data_low, "%x", &data[index]);
      index++;
      if (index == ndata) break;
    }
  }
  fclose(fp);
  g_free(str_data);

  return index;
}

/* Check if the next line is blank
 * str_data  pointer to the character string
 */
gint tool_checkSpace(gchar *str_data) {
   /*
    * check if a string contains only spaces. Returns 0 if not, 1 if yes
    */
   gint debug_mode = 0;
   gint str_len;
   gint i;
   
   str_len = strlen(str_data);
   
   for (i = 0; i < str_len - 1; i++) {
     if (debug_mode)
       g_message("Ascii value of char = 0x%x,i = %d,str_len = %d", (gint) str_data[i], i, str_len);
     if (str_data[i] != 0x20)
       return 0;
   }
   
   return 1;
}

/* Strip space characters from input character string
 * str_data  pointer to the character string
 */
gint tool_stripSpace(gchar **str_data) {

   gint debug_mode = 0;
   gint i = 0;
   
   while (*(*str_data + i * sizeof(gchar)) == 0x20) {
     if (debug_mode)
       g_message("i = %d, *str_data = %d", i, *(*str_data + i * sizeof(gchar)));
     i++;
   }
   if (debug_mode)
     g_message("i= %d, *str_data = %d", i, *(*str_data + i * sizeof(gchar)));
   *str_data = *str_data + i * sizeof(gchar);
   
   return i;
}
/* Retrieve the label of the button and return the address
 * button - Reference to the button widget
 */
gchar *gui_getButtonLabel(GtkButton *button) {
  GtkWidget *child;
  gchar *label;

  gchar *name = gtk_widget_get_name(GTK_WIDGET(button));
  if (GTK_BIN (button)->child) {
    child = GTK_BIN (button)->child;
    if (GTK_IS_LABEL(child)) {
      gtk_label_get (GTK_LABEL(child), &label);
    }
  }
  if (label == NULL) {
     g_warning("get_button_label() -> Label not present for button: %s", name);
     return NULL;
  }  
#if 0
  g_message("get_button_label() -> Label: %s", label);
#endif
  return label;
}

/* Set the label of the button
 * button - Reference to the button widget
 * new_label - The new label which replaces the existing one
 */
void gui_setButtonLabel(GtkButton *button, const gchar *new_label) {
  GtkWidget *child;
  gchar *label;

  gchar *name = gtk_widget_get_name(GTK_WIDGET(button));

  if (GTK_BIN (button)->child) {
    child = GTK_BIN (button)->child;
    if (GTK_IS_LABEL(child)) {
      gtk_label_get (GTK_LABEL(child), &label);
    }
  }
  if (label == NULL) {
    g_warning("set_button_label() -> Label not present for button: %s", name);
  }  
  else {
    gtk_label_set_text(GTK_LABEL(child), new_label);
  }
}

/* Save data in a file specified in the argument. 10 words are 
 * saved in a single line
 * filename - Name of the file
 * opt      - if 0 save one word per line
 * ndata    - Number of data words to be saved
 * data     - Data word array
 */
void tool_saveData(gchar *filename, gboolean opt, gint ndata, uint4 *data) {
  gint i;
  FILE *fp;
  if ((fp = fopen(filename, "w")) > 0) {
    for (i = 0; i < ndata; i++) {
      fprintf(fp, "%6.6x", data[i]);
      if ((opt) || (i+1)%10 == 0) fprintf(fp, "\n");
      else fprintf(fp," ");
    }
    fclose(fp);
  }
  else {
    g_warning("Error opening file %s\n", filename);
  }
}

/* Save text from the message area in a file 
 * filename - Nam eof the output file
 * text_area - reference to the Text area
 */
void gui_saveMessage(gchar *filename, const GtkWidget *text_area ) {
  FILE *fp;

  gchar *text = gtk_editable_get_chars(GTK_EDITABLE(text_area), 0, -1);
  g_message("%s", text);
  if ((fp = fopen(filename, "w")) != NULL) {
    fprintf(fp, "%s\n", text);
    fclose(fp);
  }

  g_free(text);
}
/* 
 * Load data from the input area one word perl line
 * ndata - Size of the data array
 * data  - Output data array
 */
gint svtgui_loadFromUI(GtkText *widget, gint ndata, uint4 *data) {
  gint i, index = 0;
  gchar *text = gtk_editable_get_chars(GTK_EDITABLE(widget), 0, -1);
  gchar** words = g_strsplit(text,"\n", ndata);
  for (i = 0; words[i] != NULL; i++) {
    if (words[i][0] == '/' || 
        words[i][0] == '#' ||
        words[i][0] == '!') 
      continue;
    sscanf(words[i], "%x", &data[index]);
    index++;
    if (index == ndata) break;
  }
  g_strfreev(words);
  g_free(text);

  return index;
}
/* 
 * Load data from the input area, multiple words per line
 * ndata - Size of the data array
 * data  - Output data array
 * Note: Do not start comment line anything other than '/', '#' and '!'
 */
gint svtgui_nloadFromUI(GtkText *widget, gint ndata, uint4 *data) {
  static gint MAX_LINES = 1000, MAX_WORDS_PER_LINE = 20;
  gint i, j, index = 0;
  gchar *text = gtk_editable_get_chars(GTK_EDITABLE(widget), 0, -1);
  gchar** words;
  gchar** lines = g_strsplit(text,"\n", MAX_LINES);
  for (i = 0; lines[i] != NULL; i++) {
    if (lines[i][0] == '/' || 
        lines[i][0] == '#' ||
        lines[i][0] == '!') 
      continue;
    words = g_strsplit(lines[i], " ", MAX_WORDS_PER_LINE);
    for (j = 0; words[j] != NULL; j++) {
      sscanf(words[j], "%x", &data[index]);
      printf("%x\n", data[index]);
      index++;
      if (index == ndata) break;
    }
    g_strfreev (words);
    if (index == ndata) break;
  }
  g_strfreev (lines);
  g_free(text);

  return index;
}
