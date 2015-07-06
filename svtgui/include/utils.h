#ifndef SVTGUI_UTIL_H
#define SVTGUI_UTIL_H
#include <gnome.h>

#define NEL(x) (sizeof((x))/sizeof((x)[0]))

GtkWidget *gui_createFileEntry();
GtkWidget *gui_createGnomeEntry(gint width, gint height, gint nitems, gchar *items[]);
GtkWidget *gui_createVbox(GtkWidget *app, gchar *name, gint col_spacing, gint border);
GtkWidget *gui_createHbox(GtkWidget *app, gchar *name, gint row_spacing, gint border);
GtkWidget *gui_createCombo(GtkWidget *app, gchar *name, gint width, gint height, gint border, 
                                           gint nitems, gchar *items[]);
GtkWidget *gui_createButton(GtkWidget *app, gchar *label, gchar *name, 
			                    gint width, gint height, gint border);
GtkWidget *gui_createNotebook(GtkWidget *app, gchar *name);
GtkWidget *gui_createLabel(GtkWidget *app, gchar *tag, gchar *name, gint width, gint height);
GtkWidget *gui_createTable(GtkWidget *app, gchar *name, gint row, gint column, 
                                           gint width, gint height, gint row_spacing, 
                                           gint col_spacing, gint border);
GtkWidget *gui_createIcon(GtkWidget *app, gchar *name, gchar *pix_file);
GtkWidget *gui_createFrame(GtkWidget *app, gchar *label, gchar *name, gint width, gint height);
GtkWidget *gui_createTextWidget(GtkWidget *app, gchar *name, gint width, gint height,
			                        gboolean editable);
GtkWidget *gui_createEntryWidget(GtkWidget *app, gchar *name, gint width, gint height,
                                                 gchar *text, gboolean sensitive, gboolean editable);
GtkWidget *gui_createRadioButton(GtkWidget *app, GSList *group, gchar *label, gchar *name, 
                                                 gint width, gint height, gboolean active);
GtkWidget *gui_createCheckButton(GtkWidget *app, gchar *label, gchar *name, 
                                                 gint width, gint height, gboolean active);
GtkWidget *gui_createScrollWindow(GtkWidget *app, gchar *name, gint width, gint height);
GtkWidget *gui_createStatusbar(GtkWidget *app, gchar *name);

gchar *gui_getButtonLabel(GtkButton *button);
void gui_setButtonLabel(GtkButton *button, const gchar *new_label);
void gui_eventBrowseOk(GtkWidget *widget, gpointer data);
void gui_eventSaveOk(GtkWidget *widget, gpointer data);
void gui_eventCancel(GtkWidget *widget, gpointer data);
void gui_saveMessage(gchar *filename, const GtkWidget *text_area);

gint svtgui_loadFromUI(GtkText *widget, gint ndata, uint4 *data);
gint svtgui_nloadFromUI(GtkText *widget, gint ndata, uint4 *data);
gint tool_loadFromFile(gchar *filename, gint ndata, uint4 *data);
gint tool_stripSpace(gchar **str_data);
gint tool_checkSpace(gchar *str_data);
void tool_saveData(gchar *filename, gboolean opt, gint ndata, uint4 *data);
#endif
