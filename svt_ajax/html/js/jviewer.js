var JViewer = {};
  
JViewer.getBaseURL = function() {
  var url = window.location.href;
  var indx = url.indexOf('//');
  var newurl;
  if (indx > -1)
    newurl = url.substring(indx+2, url.length);
  else
    newurl = url;

  a = newurl.split('/');
  if (indx > -1)
    url = url.substring(0,indx) + '//' + a[0];
  else
    url = a[0];

  return url;
};
JViewer.BASE_URL           = JViewer.getBaseURL()+'/cgi/mon/ajax/viewer.cgi?';
JViewer.lastURL            = "";
JViewer.canvasList         = new Array();
JViewer.slideList          = new Array();
JViewer.slideShowSpeed     = 8000;  // miliseconds
JViewer.autoUpdateInterval = 60000;  // miliseconds
JViewer.slideIndex         = 0;
JViewer.nSlides            = 0;
JViewer.slideShowTimerID   = null;
JViewer.autoUpdateTimerID  = null;
JViewer.MAX_SLIDES         = 20;
JViewer.lastMatchIndex     = -1;
JViewer.firstMatchIndex    = -1;
JViewer.pdfViewerPlugin    = false;
JViewer.DEBUG              = false;

// Ajax calls
JViewer.RequestRunList = function() {
  var qString = "command=runlist";

  var url = JViewer.BASE_URL + qString; 
  JViewer.SetProgress('visible', 'run list');
  if ($('logevent').checked) JViewer.addText('eventlogger', url);
  var retVal = new Ajax.Request(JViewer.addRandom(url), 
                                {
                                  method: 'get',
				  parameters: '',
				  onSuccess: JViewer.FillRunList
				}
    );
};
JViewer.RequestCanvasList = function() { 
  var obj = $('runoption');
  var run = obj.options[obj.selectedIndex].value;
  var qString = "command=canvaslist&view=xml&run=" + run;

  if ($('consumeronly').checked)
    qString += "&consumerlist=true";

  var url = JViewer.BASE_URL + qString; 
  if ($('logevent').checked) JViewer.addText('eventlogger', url);
  if (JViewer.IsOnline()) url = JViewer.addRandom(url);

  JViewer.SetProgress('visible', 'canvas list');
  // use cached info in case of repeated request of canvas list for the same run number
  // if the run number changed the url is different anyway
  var retVal = new Ajax.Request(url, 
                                {
                                  method: 'get',
				  parameters: '',
				  onSuccess: JViewer.FillCanvasList
				}
    );
};

// Ajax callback functions
JViewer.FillRunList = function(transport) {
  JViewer.SetProgress('hidden');
  try {
    var response = eval('(' + transport.responseText + ')');
    var rows = response.runs;
    JViewer.FillSelectBoxItems(rows,    'runoption', 0);
    JViewer.FillSelectBoxItems(rows, 'refrunoption', 1);
  }
  catch (err) {
    JViewer.ShowError("Error detail: " + err.message);
  }
};
JViewer.FillCanvasList = function(transport) {
  JViewer.SetProgress('hidden');
  try {
    var doc = transport.responseXML;
    var root = doc.documentElement;
    JViewer.FillList(root);
  }
  catch (err) {
    JViewer.ShowError("Error detail: " + err.message);
  }
};
JViewer.RequestPlots = function() { 
  // Handle a single canvas name as input
  var path = '';
  var args = arguments[0];
  if (args != null && args.length > 0) 
    path = args;

  // EXPERIMENTAL - applies only to online histogram browsing
  // A new plot has been requested, so uninstall autoupdate
  if (JViewer.IsOnline()) JViewer.StopAutoUpdate();

  // We're using GET method
  var run = JViewer.GetRun();
  var qString = 'command=plot&run='+run;
  qString += JViewer.GetCanvasProperties();
   
  // Collect canvas names
  if (path.length == 0) {
    var retval = JViewer.GetSelectedCanvasList();
    if (retval == false) return;
    // Make sure that too many slides are not requested by mistake
    if (JViewer.nSlides > JViewer.MAX_SLIDES) {
      JViewer.ShowError("Too many slides selected!, nSlide=" 
         + JViewer.nSlides + " reasonable only for slideshow");
      return;
    }
  }

  // Form canvas list
  if (path.length != 0) {
    qString += '&canvas='+path;
    JViewer.nSlides = 1;
  }
  else {
    for (var i = 0; i < JViewer.nSlides; i++) 
      qString += '&canvas=' + JViewer.canvasList[i];
  }

  // logy option
  if ($('logy').checked) qString += '&logy=true';
   
  // Is histogram Fitting requested? 
  if ($('enablefit').checked) {
    qString += '&reqfit=true';
   
    // Fit method
    var obj = $('fitfunction');
    if (obj == null) {
      JViewer.ShowError("Fit Function option object not found!");
      return;
    }
    var value = obj.options[obj.selectedIndex].value;
    if (value == "user") {
      // we disregard 'user' function option for the moment
    }
    else
      qString += '&fitfunction='+value;
  
    // Fit Range as Comma Separated float values (CSV)
    obj = $('fitrange');
    if (obj) qString += '&fitrange='+obj.value;
   
    // Initial Fit Parameters 
    obj = $('fitparams');
    if (obj) qString += '&fitpar='+obj.value;
  }
   
  // Rows and columns
  if (JViewer.nSlides > 1 ) {
    var r = 2;
    var c = 2;
    if ($('multiple').checked) {
      obj = $('rows');
      if (obj) r = parseInt(obj.value);
      if (isNaN(r)) r = 2;
    
      obj = $('cols');
      if (obj) c = parseInt(obj.value);
      if (isNaN(c)) c = 2;
    }
    qString += '&rows=' + r + '&cols=' + c;
  }
  if (JViewer.nSlides == 1 || $('forcexrange').checked) {
    obj = $('lrange');
    var value = parseFloat(obj.value);
    if (!isNaN(value)) qString += '&xmin=' + value;
   
    obj = $('urange');
    value = parseFloat(obj.value);
    if (!isNaN(value)) qString += '&xmax=' + value;
    if ($('comparison').checked) {
      qString = qString.replace("command=plot", "command=compare");
      obj = $('refrunoption');
      value = obj.options[obj.selectedIndex].value;
      qString += "&refrun="+value;
    }
  }
   
  var url = JViewer.BASE_URL + qString;
  JViewer.lastURL = url;
  if (JViewer.IsOnline()) url = JViewer.addRandom(url);

  var canvas = $('drawingcanvas');
  if (canvas == null) {
    JViewer.ShowError("Canvas obj is not found!");
    return;
  }
  if ($('logevent').checked) JViewer.addText('eventlogger', url);
  canvas.src = url; 
   
  // EXPERIMENTAL
  // reinstall autoupdate
  if (JViewer.IsOnline()) {
    var obj = $('autoupdateopt');
    if (obj != null && !obj.checked) obj.checked = true;
    JViewer.StartAutoUpdate();
  }
}; 

JViewer.FillList = function(root) {
  // Canvas list area select box
  var listview = $('canvaslistarea');
  if (listview == null) {
    JViewer.ShowError('Canvas list Object, not found!');
    return;
  }
  listview.options.length = 0;  // Reset

  // Create the outer-most element of the tree-view
  var theUL = document.createElement('UL');

  // Set attribute required by the tree-view package
  theUL.setAttribute('id',   "dhtmlgoodies_tree");
  theUL.setAttribute('class',"dhtmlgoodies_tree");

  // Now traverse the xml received from the server
  // On the fill items in canvas list area select box as well
  JViewer.traverse(root, theUL, listview);

  // Now set the outer-most tree-view element that holds
  // the full tree as the first 
  // child of a div 
  var div = $('treeviewid');
  if (div == null) return;
  div.removeChild(div.firstChild); 
  div.appendChild(theUL);

  // Build the tree now, the order is relevant
  initTree(); 
  if ($('logdebug').checked) JViewer.addText('debuglogger', theUL.innerHTML);
};

JViewer.traverse = function(node, element, listview) {
  for (var i = 0; i < node.childNodes.length; i++) {
    var obj = node.childNodes.item(i);
    if (obj.nodeType != 1) continue;

    var aLi = document.createElement('LI');
    var aTag = document.createElement('A');
    aTag.setAttribute('href',"#");
    var text; 
    if (obj.nodeName == 'canvas') {
      var name = obj.textContent;
      var path = JViewer.GetPath(obj);
      text = document.createTextNode(name);
      aTag.setAttribute('class', "leaf");
      aTag.setAttribute('name', path+'/'+name);
      aTag.setAttribute('onclick', "JViewer.PlotAction(this)");

      JViewer.FillSelectBoxItem(listview, name, path);       
    }
    else {
      text = document.createTextNode(obj.nodeName);
    }
    aTag.appendChild(text);
    aLi.appendChild(aTag);
    if (obj.nodeName != 'canvas') {
      aLi.setAttribute('class',"dir");
      if (JViewer.hasChildNode(obj)) {
        var aUl = document.createElement('UL');
        JViewer.traverse(obj, aUl, listview);
        aLi.appendChild(aUl);
      }
    }
    element.appendChild(aLi);
  }  
};
JViewer.hasChildNode = function(node) {
  for (var i = 0; i < node.childNodes.length; i++) {
    var obj = node.childNodes.item(i);
    if (obj.nodeType != 1) continue;
    return true;
  }
  return false;
};

JViewer.GetSelectBoxValue = function(id) {
  var obj = $(id);
  if (obj == null) return '?';
  var value = obj.options[obj.selectedIndex].value;
  return value;
};
JViewer.GetRun = function() {
  return JViewer.GetSelectBoxValue('runoption');
  //var obj = $('runoption');
  //if (obj == null) return '?';
  //var value = obj.options[obj.selectedIndex].value;
  //return value;
};
JViewer.GetSelectedCanvasList = function() {
  var obj = $('canvaslistarea');
  var len = obj.length;
  if (len == 0) {
    if (JViewer.DEBUG) JViewer.ShowError("Canvas List Area empty!");
    return false;
  }
  
  var k = 0;
  for (var i = 0; i < len; i++) {
    if (obj.options[i].selected) {
      var val = obj.options[i].value + '/' + obj.options[i].text;
      JViewer.canvasList[k] = val;
      k++;
    }
  }
  JViewer.nSlides = k;  // DO NOT use canvasList.length because we do not reset it correctly
  if (JViewer.nSlides == 0) {
    JViewer.ShowError("No canvas name selected!");
    return false;
  }

  return true;
};
JViewer.StopAutoUpdate = function() {
  if (JViewer.IsOnline() && JViewer.autoUpdateTimerID != null) {
    clearTimeout(JViewer.autoUpdateTimerID); 
    JViewer.autoUpdateTimerID = null;
  }
};
JViewer.GetCanvasProperties = function() {
  // Get Canavs properties
  var text = '';
  var canvas = $('drawingcanvas');
  if (canvas == null) {
    JViewer.ShowError("Canvas obj is not found!");
    return text;
  }
  text = '&width='+canvas.width+'&height='+canvas.height;
  return text;
};
JViewer.RunSlideShow = function() {
  if (!$('slideshow').checked) {
    JViewer.addText('errorlogger', 'Please check the slideshow option checkbox!');
    return;
  }

  // We're using GET method
  var run = JViewer.GetRun();
  var qString = 'command=plot&run='+run;
  qString += JViewer.GetCanvasProperties();

  if (JViewer.GetSelectedCanvasList() == false) return;
  if (JViewer.nSlides > 1) {
    for (var i = 0; i < JViewer.canvasList.length; i++)
      JViewer.slideList[i] = qString + '&canvas=' + JViewer.canvasList[i];
    JViewer.StartSlideShow();  
  }
};
// Slideshow functionalities
JViewer.StartSlideShow = function() {
  JViewer.setSlide(JViewer.slideIndex);
  JViewer.slideIndex = (JViewer.slideIndex+1) % JViewer.nSlides;
  JViewer.slideShowTimerID 
     = setTimeout('JViewer.StartSlideShow()', JViewer.slideShowSpeed);
};
JViewer.StopSlideShow = function() {
  if (JViewer.slideShowTimerID != null) {
    if ($('logevent').checked) JViewer.addText('eventlogger', 'Stopping slideshow');
    JViewer.clearTimeout(JViewer.slideShowTimerID); 
    JViewer.slideShowTimerID = null;
  }
};
JViewer.ShowFirst = function() {
  JViewer.setSlide(0);
};
JViewer.ShowLast = function() {
  JViewer.setSlide(nSlides - 1);
};
JViewer.ShowPrev = function() {
  JViewer.slideIndex = (JViewer.slideIndex - 1) % JViewer.nSlides;
  if (JViewer.slideIndex < 0) JViewer.slideIndex = JViewer.nSlides - 1;
  JViewer.setSlide(JViewer.slideIndex);
};
JViewer.ShowNext = function() {
  JViewer.slideIndex = (JViewer.slideIndex+1) % JViewer.nSlides;
  JViewer.setSlide(JViewer.slideIndex);
};
JViewer.setSlide = function(index) {
  if (JViewer.nSlides == 0) {
    JViewer.ShowError("No canvas name selected!");
    return false;
  }
  var canvas = $('drawingcanvas');
  if (canvas == null) return;
  var url = JViewer.BASE_URL + JViewer.slideList[index]; 
  if (JViewer.IsOnline()) url = JViewer.addRandom(url);
  canvas.src = url; 
};
JViewer.ListSelectAll = function(option) {
  var obj = $('canvaslistarea');
  if (obj == null) {
    JViewer.ShowError("List area object null!!");
    return false;
  }
  for (var i = 0; i < obj.length; i++)
    obj.options[i].selected = (option == "select") ? true : false;

  return true;
};
JViewer.FindItem = function() {
  // Find pattern from the text input
  var obj = $('finditem');
  if (obj == null) return false;
  var patt = obj.value;
  if (patt == "") return false;

  // Now find the canvas list area object
  obj = $('canvaslistarea');
  if (obj == null) {
    JViewer.ShowError("List area object null!!");
    return false;
  }
  var len = obj.length;

  for (var i = 0; i < len; i++) {
    var val = obj.options[i].text; // value;
    var loc = val.indexOf(patt);
    if (loc != -1 && i > JViewer.lastMatchIndex) {
      if (JViewer.lastMatchIndex == -1) JViewer.firstMatchIndex = i;
      if (JViewer.lastMatchIndex > -1)
        obj.options[JViewer.lastMatchIndex].selected = false;
      obj.options[i].selected = true;
      JViewer.lastMatchIndex = i;

      break;
    }
    else if (i == len-1) {
      if (JViewer.lastMatchIndex > -1)
        obj.options[JViewer.lastMatchIndex].selected = false;
      if (JViewer.firstMatchIndex > -1)
        obj.options[JViewer.firstMatchIndex].selected = true;
      JViewer.lastMatchIndex = -1;
    }
  }
  return true;
};
JViewer.SetSelectBoxIndex = function(id, name) {
  var obj = $(id);
  if (obj == null) return;

  var index = -1;
  obj.selectedIndex = index;
  var len = obj.length;
  for (var i = 0; i < len; i++) {
    var val = obj.options[i].value + '/' + obj.options[i].text;
    if (val == name) {
      index = i;
      break;
    }
  }
  if (index > -1) {
    obj.selectedIndex = index;
    return true;
  }
  else
    return false;
};
JViewer.EnablePanel = function() {
  var fobj = $('fitfunction');
  var robj = $('fitrange');
  var eobj = $('fitexp');
  var pobj = $('fitparams');
  if (fobj == null || robj == null || eobj == null || pobj == null) {
    JViewer.ShowError('one of the fit panel objects null!');
    return;
  }
  
  if ($('enablefit').checked) {
    fobj.disabled = false;
    robj.disabled = false;
    pobj.disabled = false;
    JViewer.IsUserFunction();
  }
  else {
    fobj.disabled = true;
    robj.disabled = true;
    eobj.disabled = true;
    pobj.disabled = true;
  }
};
JViewer.IsUserFunction = function() {
  var fobj = $('fitfunction');
  var eobj = $('fitexp');
  if (fobj == null || eobj == null) return; 

  var value = fobj.options[fobj.selectedIndex].value;
  if (value == "user") {
    eobj.disabled = false;
  }
  else {
    eobj.disabled = true;
  }
};
JViewer.RunMacro = function() {
  // Get Canavs
  var canvas = $('drawingcanvas');
  if (canvas == null) {
    JViewer.ShowError("Canvas obj is not found!");
    return;
  }

  // Macro list
  var obj = $('macrolistarea');
  if (obj == null) return;

  var value = obj.options[obj.selectedIndex].value;
  var qString = "command=macro&run=" + JViewer.GetRun() + "&name=" + value;
  var url = JViewer.BASE_URL + qString; 
  JViewer.lastURL = url;
  if ($('logevent').checked) JViewer.addText('eventlogger', url);
  if (JViewer.IsOnline()) url = JViewer.addRandom(url);
  canvas.src = url; 
};
JViewer.ShowPDF = function() {
  if (JViewer.lastURL == null || JViewer.lastURL.length <= 0) {
    JViewer.ShowError('lastURL not set, lastURL='+JViewer.lastURL);
    return; 
  }
  var url = JViewer.lastURL + '&getpdf=true';
  var savePDF = (JViewer.GetSelectBoxValue('showpdfoption') == 'ask'); 
  if (savePDF)  url += '&savepdf=true';
  
  if (JViewer.pdfViewerPlugin) url += '&plugin=true';
  if ($('logevent').checked) JViewer.addText('eventlogger', url);

  // open a new window and then re-direct 
  var nwin = window.open(url, '_blank', 'left=20,top=20,width=500,height=100,toolbar=1,resizable=0');
};
JViewer.RefreshPlot = function() {
  if (!JViewer.IsOnline()) return;
  if (JViewer.lastURL == null || JViewer.lastURL.length <= 0) return; 
  JViewer.uncheckSlideShow();
    
  // Get Canavs
  var canvas = $('drawingcanvas');
  if (canvas == null) {
    JViewer.ShowError("Canvas obj is not found!");
    return;
  }
  canvas.src = JViewer.addRandom(JViewer.lastURL); 
};
JViewer.StartAutoUpdate = function() {
  JViewer.RefreshPlot();
  JViewer.autoUpdateTimerID 
     = setTimeout('JViewer.StartAutoUpdate()', JViewer.autoUpdateInterval);
};
JViewer.AutoUpdate = function() {
  if ($('autoupdateopt').checked) {
    JViewer.StartAutoUpdate();
  }
  else {
    JViewer.StopAutoUpdate(); 
  }
};
JViewer.CheckPlugin = function() {
  var len = navigator.plugins.length;
  for (var i = 0; i < len; i++) {
    if (JViewer.DEBUG) 
      JViewer.ShowError(navigator.plugins[i].name + ", " + navigator.plugins[i].description);
    var name = navigator.plugins[i].name;
    var desc = navigator.plugins[i].description;
    if ((name.indexOf("Adobe Reader") != -1 && desc.indexOf("PDF") != -1) ||
        (name.indexOf("MozPlugger")   != -1 && desc.indexOf("MozPlugger") != -1) )
    {
      JViewer.pdfViewerPlugin = true;
    }
  }
};
JViewer.uncheckSlideShow = function() {
  var show = $('slideshow');
  if (show != null && show.checked) show.checked = false;
};
JViewer.RedoCurrentPlots = function() {
  if (!JViewer.IsOnline()) return;
  JViewer.uncheckSlideShow();

  JViewer.RequestPlots();
};
JViewer.FillSelectBoxItem = function(obj, name, path) {
  var option = new Option(name, path);
  option.title = path;
  try {
    obj.add(option, null);
  }
  catch (e) {
    obj.add(option, -1);
  }
};
JViewer.FillSelectBoxItems = function(rows, destObj, index) {
  // Run number select box
  var obj = $(destObj);
  if (obj == null) {
    JViewer.ShowError('Run option Object, ' + destObj + ' not found!');
    return;
  }
  obj.options.length = 0;

  for (var i = 0; i < rows.length; i++) {
    var name = rows[i];
    var option = new Option(name, name);
    try {
      obj.add(option, null);
    }
    catch (e) {
      obj.add(option, -1);
    }
  }
  if (index < obj.options.length)
    obj.options[index].selected = true;
};
JViewer.PlotAction = function(obj) {
  if (obj == null) return;
  var path = obj.name;
  JViewer.SetSelectBoxIndex('canvaslistarea', path);
  JViewer.RequestPlots(path);
};
JViewer.RequestSinglePlot = function(obj) {
  var index = obj.selectedIndex;
  if (index < 0) return;
  var name  = obj.options[index].value + '/' + obj.options[index].text;
  JViewer.RequestPlots(name);
};
JViewer.SetProgress = function(option) {
  var progress = $('progressbar');
  if (progress == null) return;
  var label = $('datatypeid');
  if (label == null) return;
  var args = arguments[1];
  if (args != null && args.length > 0) {
    label.innerHTML = 'Please wait, loading '+args;
  }
  progress.style.visibility = option; // "visible" or "hidden"
};
JViewer.randOrd =  function () {
  return (Math.round(Math.random())-0.5); 
};
JViewer.IsOnline =  function() {
  if (JViewer.GetRun() == "online") return true;
  return false;
};
JViewer.GetPath = function(node) {
  var pathList = new Array;
  var parent = node.parentNode;
  while (parent && parent.nodeName != '#document') {
    pathList.push(parent.nodeName);
    parent = parent.parentNode;
  }
  pathList.reverse();
  return pathList.join('/');
};
JViewer.SetConsumerOnly = function() {
  var dobj = $('treeviewid'); // div containing the treeview
  if (dobj == null) return
  if ($('consumeronly').checked) {
    dobj.style.visibility = 'hidden';
  }
  else {
    dobj.style.visibility = 'visible';
  }
};
// Utility functions
JViewer.stopRKey = function(evt) {
  var evt = (evt) ? evt : ((event) ? event : null);
  var node = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
  if ((evt.keyCode == 13) && (node.type == "text")) {return false;}
};
JViewer.addRandom = function(url) {
  return (url + '&t=' + Math.random());
};
JViewer.addText = function(element, text) {
  var obj = $(element);
  if (obj == null) return;
  var d = new Date();
  var m = d.getMonth() + 1;
  var now = d.getFullYear() + '/' + m + '/' + d.getDate() 
    + '-' + d.getHours() +':' + d.getMinutes() +':' + d.getSeconds();
  obj.value += ((obj.value == '') ? '' : "\n") + now + " > " + text;
};
JViewer.clearText = function(element) {
  var obj = $(element);
  if (obj == null) return;
  obj.value = '';
};
JViewer.ShowError = function(message) {
  JViewer.addText('errorlogger', message);
  showTab('dhtmlgoodies_tabView3', 1);
};
JViewer.UncheckBox = function(id) {
  var obj = $(id);
  if (obj == null) return false;
  obj.checked = false;
  return true;
};
JViewer.ShowHideLoggerPanel = function() {
  var obj = $('showlogger');
  if (obj == null) return false;
  JViewer.ShowHidePanel('loggerpanel', (obj.checked) ? 'visible' : 'hidden');
};
JViewer.ShowHidePanel = function(id, option) {
  var obj = $(id);
  if (obj == null) return false;
  obj.style.visibility = option;
  return true;
};
JViewer.ClearLogger = function() {
  JViewer.clearText('eventlogger');
  JViewer.clearText('errorlogger');
  JViewer.clearText('debuglogger');
}
// input field's event handlers 
window.onload = function() {
  document.onkeypress = JViewer.stopRKey;

  var m = initTabs('dhtmlgoodies_tabView1', new Array('List View','Tree View'), 1,'99.5%','100%');
  var n = initTabs('dhtmlgoodies_tabView2', new Array('Options','Fitting','Macro'), 0,'99.5%','100%');
  var p = initTabs('dhtmlgoodies_tabView3', new Array('Event Logger','Error Logger','Debug Logger'), 1,'99.5%','100%');
  var cbList = Array('autoupdateopt',
                     'consumeronly',
                     'slideshow', 
                     'logy', 
                     'forcexrange', 
                     'comparison', 
                     'multiple', 
                     'enablefit');
  for (var i = 0; i < cbList.length; i++)
    JViewer.UncheckBox(cbList[i]);

  JViewer.ClearLogger();

  // Tackle MSIE
  var agt = navigator.userAgent.toLowerCase();
  if (agt.indexOf('msie') != -1) {
    if ($('logdebug').checked)
      JViewer.addText('debuglogger', "Does not work that well on IE! Please use Firefox.- Subir");
    var obj = $('divimg');
    if (obj != null) obj.style.width = '55%';

    obj = $('drawingcanvas');
    if (obj != null) obj.style.width = '95%';

    obj = $('getpdf');
    if (obj != null) obj.style.marginLeft = '83%';

    obj = $('finditem');
    if (obj != null) obj.style.width = '50%';
  }
  JViewer.EnablePanel();
  if (agt.indexOf('msie') == -1) JViewer.CheckPlugin();

  // this takes time 
  JViewer.RequestRunList();
}; 
window.onunload = function() { 
  JViewer.StopAutoUpdate();
  JViewer.StopSlideShow();
};
