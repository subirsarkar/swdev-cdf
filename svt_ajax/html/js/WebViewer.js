var WebViewer = {};
  
WebViewer.request = null; 
WebViewer.getBaseURL = function() {
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
WebViewer.BASE_URL           = WebViewer.getBaseURL()+'/cgi/mon/ajax/viewer.cgi?';
WebViewer.lastURL            = "";
WebViewer.canvasList         = new Array();
WebViewer.slideList          = new Array();
WebViewer.slideShowSpeed     = 8000;  // miliseconds
WebViewer.autoUpdateInterval = 60000;  // miliseconds
WebViewer.index              = 0;
WebViewer.nSlides            = 0;
WebViewer.slideShowTimerID   = null;
WebViewer.autoUpdateTimerID  = null;
WebViewer.MAX_SLIDES         = 20;
WebViewer.lastMatchIndex     = -1;
WebViewer.firstMatchIndex    = -1;
WebViewer.pdfViewerPlugin    = false;
WebViewer.DEBUG              = false;

// ------------------------------------------------
// From AJAX Hacks, Bruce W. Perry, O'Reilly, 2005
// -----------------------------------------------
// Wrapper function for constructing a request object. 
// Parameters: 
//   <reqType>: The HTTP request type, such as GET or POST. 
//   <url>: The URL of the server program. 
//   <asynch>: Whether to send the request asynchronously or not. 
//   <respHandle>: The name of the function that will handle the response. 
//
//   Any fifth parameters, represented as arguments[4], are the data a 
//   POST request is designed to send. 
// ------------------------------------------------------------------ 
WebViewer.httpRequest = function(reqType, url, asynch, respHandle) { 
  if (window.XMLHttpRequest)   // Mozilla-based browsers 
    WebViewer.request = new window.XMLHttpRequest(); 
  else if (window.ActiveXObject) { 
    WebViewer.request = new window.ActiveXObject("Msxml2.XMLHTTP"); 
    if (!WebViewer.request)
      WebViewer.request = new window.ActiveXObject("Microsoft.XMLHTTP"); 
  } 
  // very unlikely, but we test for a null request 
  // if neither ActiveXObject was initialized 
  if (WebViewer.request) { 
    // if the reqType parameter is POST, then the 
    // 5th argument to the function is the POSTed data 
    if (reqType.toLowerCase() != "post") { 
      WebViewer.initReq(reqType, url, asynch, respHandle); 
    }  
    else { 
      // the POSTed data 
      var args = arguments[4]; 
      if (args != null && args.length > 0)
         WebViewer.initReq(reqType, url, asynch, respHandle, args); 
    } 
  } 
  else { 
    WebViewer.ShowError("Your browser does not permit the use of all " + 
                        "of this application's features!"); 
  } 
};

// Initialize a request object that is already constructed 
WebViewer.initReq = function(reqType, url, bool, respHandle) {
  try { 
    // Specify the function that will handle the HTTP response 
    WebViewer.request.onreadystatechange = respHandle; 
    WebViewer.request.open(reqType, url, bool); 
  
    // if the reqType parameter is POST, then the 
    // 5th argument to the function is the POSTed data 
    if (reqType.toLowerCase() == "post") { 
      WebViewer.request.setRequestHeader("Content-Type", 
           "application/x-www-form-urlencoded; charset=UTF-8"); 
      WebViewer.request.send(arguments[4]); 
    }  
    else { 
      WebViewer.request.send(null); 
    } 
  } 
  catch (errv) { 
    WebViewer.ShowError ( 
        "The application cannot contact " + 
        "the server at the moment. " + 
        "Please try again in a few seconds.\\n" + 
        "Error detail: " + errv.message); 
  } 
};

WebViewer.RequestRunList = function() {
  var queryString = "command=runlist";
  WebViewer.addText('eventlogger', WebViewer.BASE_URL+queryString);

  var url = WebViewer.BASE_URL + escape(queryString); 
  WebViewer.SetProgress('visible', 'run list');
  WebViewer.httpRequest("GET", WebViewer.addRandom(url), true, WebViewer.FillRunList); 
};

WebViewer.FillRunList = function() {
  if (WebViewer.request.readyState == 4) {
    WebViewer.SetProgress('hidden');
    if (WebViewer.request.status == 200) {
      try {
        var response = eval('(' + WebViewer.request.responseText + ')');
        var rows = response.runs;
        WebViewer.FillSelectBoxItems(rows,    'runoption', 0);
        WebViewer.FillSelectBoxItems(rows, 'refrunoption', 1);
      }
      catch (err) {
        WebViewer.ShowError("Error detail: " + err.message);
      }
    } 
    else {
      WebViewer.ShowError("FillRunList:  ERROR:" + WebViewer.request.readyState 
                                          + ", " + WebViewer.request.status);
    }
  }
};
WebViewer.RequestCanvasList = function() { 
  var obj = document.getElementById("runoption");
  var run = obj.options[obj.selectedIndex].value;
  var queryString = "command=canvaslist&view=xml&run=" + run;

  if (document.getElementById("consumeronly").checked)
    queryString += "&consumerlist=true";
  WebViewer.addText('eventlogger', WebViewer.BASE_URL + queryString);

  var url = WebViewer.BASE_URL + escape(queryString); 
  if (WebViewer.IsOnline()) url = WebViewer.addRandom(url);

  WebViewer.SetProgress('visible', 'canvas list');
  WebViewer.httpRequest("GET", url, true, WebViewer.FillCanvasList); 
};
WebViewer.FillCanvasList = function() {
  if (WebViewer.request.readyState == 4) {
    WebViewer.SetProgress('hidden');
    if (WebViewer.request.status == 200) {
      try {
        var doc = WebViewer.request.responseXML;
        var root = doc.documentElement;
        WebViewer.FillList(root);
      }
      catch (err) {
        WebViewer.ShowError("Error detail: " + err.message);
      }
    } 
    else {
      WebViewer.ShowError("FillCanvasList:  ERROR:" + WebViewer.request.readyState
                                             + ", " + WebViewer.request.status); 
    }
  }
};
WebViewer.FillList = function(root) {
  // Canvas list area select box
  var listview = document.getElementById('canvaslistarea');
  if (listview == null) {
    WebViewer.ShowError('Canvas list Object, not found!');
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
  WebViewer.traverse(root, theUL, listview);

  // Now set the outer-most tree-view element that holds
  // the full tree as the first 
  // child of a div 
  var div = document.getElementById('treeviewid');
  if (div == null) return;
  div.removeChild(div.firstChild); 
  div.appendChild(theUL);

  // Build the tree now, the order is relevant
  initTree(); 
  if (WebViewer.DEBUG) WebViewer.addText('debuglogger', theUL.innerHTML);
};

WebViewer.traverse = function(node, element, listview) {
  for (var i = 0; i < node.childNodes.length; i++) {
    var obj = node.childNodes.item(i);
    if (obj.nodeType != 1) continue;

    var aLi = document.createElement('LI');
    var aTag = document.createElement('A');
    aTag.setAttribute('href',"#");
    var text; 
    if (obj.nodeName == 'canvas') {
      var name = obj.textContent;
      var path = WebViewer.GetPath(obj);
      text = document.createTextNode(name);
      aTag.setAttribute('class', "leaf");
      aTag.setAttribute('name', path+'/'+name);
      aTag.setAttribute('onclick', "WebViewer.PlotAction(this)");

      WebViewer.FillSelectBoxItem(listview, name, path);       
    }
    else {
      text = document.createTextNode(obj.nodeName);
    }
    aTag.appendChild(text);
    aLi.appendChild(aTag);
    if (obj.nodeName != 'canvas') {
      aLi.setAttribute('class',"dir");
      if (WebViewer.hasChildNode(obj)) {
        var aUl = document.createElement('UL');
        WebViewer.traverse(obj, aUl, listview);
        aLi.appendChild(aUl);
      }
    }
    element.appendChild(aLi);
  }  
};
WebViewer.hasChildNode = function(node) {
  for (var i = 0; i < node.childNodes.length; i++) {
    var obj = node.childNodes.item(i);
    if (obj.nodeType != 1) continue;
    return true;
  }
  return false;
};

// In case we want to send the html of the tree view
// directly from the server we use the following
WebViewer.RequestTreeView = function() { 
  var run = WebViewer.GetRun();
  if (run == '?') return;

  var queryString = "command=canvaslist&view=tree&run=" + run;
  var url = WebViewer.BASE_URL + escape(queryString); 
  if (WebViewer.IsOnline()) url = WebViewer.addRandom(url);

  WebViewer.httpRequest("GET", WebViewer.addRandom(url), true, WebViewer.FillTreeView); 
};
WebViewer.FillTreeView = function() {
  if (WebViewer.request.readyState == 4) {
    WebViewer.SetProgress('hidden');
    if (WebViewer.request.status == 200) {
      try {
        var data = WebViewer.request.responseText;
        document.getElementById('treeviewid').innerHTML = data;
        initTree();
      }
      catch (err) {
        WebViewer.ShowError("Error detail: " + err.message);
      }
    } 
    else {
      WebViewer.ShowError("FillTreeView: ERROR: " + WebViewer.request.readyState 
                                           + ", " + WebViewer.request.status); 
    }
  }
};
WebViewer.GetRun = function() {
  var obj = document.getElementById("runoption");
  if (obj == null) return '?';
  var value = obj.options[obj.selectedIndex].value;
  return value;
};
WebViewer.GetSelectedCanvasList = function() {
  var obj = document.getElementById("canvaslistarea");
  var len = obj.length;
  if (len == 0) {
    WebViewer.ShowError("Canvas List Area empty!");
    return false;
  }
  
  var k = 0;
  for (var i = 0; i < len; i++) {
    if (obj.options[i].selected) {
      var val = obj.options[i].value + '/' + obj.options[i].text;
      WebViewer.canvasList[k] = val;
      k++;
    }
  }
  WebViewer.nSlides = k;  // DO NOT use canvasList.length because we do not reset it correctly
  if (WebViewer.nSlides == 0) {
    WebViewer.ShowError("No canvas name selected!");
    return false;
  }

  return true;
};
WebViewer.StopAutoUpdate = function() {
  if (WebViewer.IsOnline() && WebViewer.autoUpdateTimerID != null) {
    clearTimeout(WebViewer.autoUpdateTimerID); 
    WebViewer.autoUpdateTimerID = null;
  }
};
WebViewer.GetCanvasProperties = function() {
  // Get Canavs properties
  var text = '';
  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) {
    WebViewer.ShowError("Canvas obj is not found!");
    return text;
  }
  text = '&width='+canvas.width+'&height='+canvas.height;
  return text;
};
WebViewer.RunSlideShow = function() {
  if (!document.getElementById('slideshow').checked) {
    WebViewer.addText('errorlogger', 'Please check the slideshow option checkbox!');
    return;
  }

  // We're using GET method
  var run = WebViewer.GetRun();
  var queryString = 'command=plot&run='+run;
  queryString += WebViewer.GetCanvasProperties();

  if (WebViewer.GetSelectedCanvasList() == false) return;
  if (WebViewer.nSlides > 1) {
    for (var i = 0; i < WebViewer.canvasList.length; i++)
      WebViewer.slideList[i] = queryString + '&canvas=' + escape(WebViewer.canvasList[i]);
    WebViewer.StartSlideShow();  
  }
};
WebViewer.RequestPlots = function() { 
  // Handle a single canvas name as input
  var plot = '';
  var args = arguments[0];
  if (args != null && args.length > 0) 
    plot = args;

  // EXPERIMENTAL - applies only to online histogram browsing
  // A new plot has been requested, so uninstall autoupdate
  if (WebViewer.IsOnline()) WebViewer.StopAutoUpdate();

  // We're using GET method
  var run = WebViewer.GetRun();
  var queryString = 'command=plot&run='+run;
  queryString += WebViewer.GetCanvasProperties();
   
  // Collect canvas names
  if (plot.length == 0) {
    var retval = WebViewer.GetSelectedCanvasList();
    if (retval == false) return;
    // Make sure that too many slides are not requested by mistake
    if (WebViewer.nSlides > WebViewer.MAX_SLIDES) {
      WebViewer.ShowError("Too many slides selected!, nSlide=" 
         + WebViewer.nSlides + " reasonable only for slideshow");
      return;
    }
  }

  // Form canvas list
  if (plot.length != 0) {
    queryString += '&canvas='+plot;
    WebViewer.nSlides = 1;
  }
  else {
    for (var i = 0; i < WebViewer.nSlides; i++) 
      queryString += '&canvas=' + WebViewer.canvasList[i];
  }

  // logy option
  if (document.getElementById('logy').checked) queryString += '&logy=true';
   
  // Is histogram Fitting requested? 
  if (document.getElementById('enablefit').checked) {
    queryString += '&reqfit=true';
   
    // Fit method
    var obj = document.getElementById('fitfunction');
    if (obj == null) {
      WebViewer.ShowError("Fit Function option object not found!");
      return;
    }
    var value = obj.options[obj.selectedIndex].value;
    if (value == "user") {
      // we disregard 'user' function option for the moment
    }
    else
      queryString += '&fitfunction='+value;
  
    // Fit Range as Comma Separated float values (CSV)
    obj = document.getElementById('fitrange');
    if (obj) queryString += '&fitrange='+obj.value;
   
    // Initial Fit Parameters 
    obj = document.getElementById('fitparams');
    if (obj) queryString += '&fitpar='+obj.value;
  }
   
  // Rows and columns
  if (WebViewer.nSlides > 1 ) {
    var r = 2;
    var c = 2;
    if (document.getElementById("multiple").checked) {
      obj = document.getElementById("rows");
      if (obj) r = parseInt(obj.value);
      if (isNaN(r)) r = 2;
    
      obj = document.getElementById("cols");
      if (obj) c = parseInt(obj.value);
      if (isNaN(c)) c = 2;
    }
    queryString += '&rows=' + r + '&cols=' + c;
  }
  if (WebViewer.nSlides == 1 || document.getElementById("forcexrange").checked) {
    obj = document.getElementById("lrange");
    var value = parseFloat(obj.value);
    if (!isNaN(value)) queryString += '&xmin=' + value;
   
    obj = document.getElementById("urange");
    value = parseFloat(obj.value);
    if (!isNaN(value)) queryString += '&xmax=' + value;
    if (document.getElementById("comparison").checked) {
      queryString = queryString.replace("command=plot", "command=compare");
      obj = document.getElementById("refrunoption");
      value = obj.options[obj.selectedIndex].value;
      queryString += "&refrun="+value;
    }
  }
   
  var url = WebViewer.BASE_URL + escape(queryString);
  lastURL = url;
  if (WebViewer.IsOnline()) url = WebViewer.addRandom(url);

  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) {
    WebViewer.ShowError("Canvas obj is not found!");
    return;
  }
  WebViewer.addText('eventlogger', WebViewer.BASE_URL + queryString);
  canvas.src = url; 
   
  // EXPERIMENTAL
  // reinstall autoupdate
  if (WebViewer.IsOnline()) {
    var obj = document.getElementById("autoupdateopt");
    if (obj != null && !obj.checked) obj.checked = true;
    WebViewer.StartAutoUpdate();
  }
}; 

// Slideshow functionalities
WebViewer.StartSlideShow = function() {
  WebViewer.setSlide(index);
  index = (index+1) % WebViewer.nSlides;
  WebViewer.slideShowTimerID 
     = setTimeout('WebViewer.StartSlideShow()', WebViewer.slideShowSpeed);
};
WebViewer.StopSlideShow = function() {
  if (WebViewer.slideShowTimerID != null) {
    WebViewer.addText('eventlogger', 'Stopping slideshow');
    WebViewer.clearTimeout(WebViewer.slideShowTimerID); 
    WebViewer.slideShowTimerID = null;
  }
};
WebViewer.ShowFirst = function() {
  WebViewer.setSlide(0);
};
WebViewer.ShowLast = function() {
  WebViewer.setSlide(nSlides - 1);
};
WebViewer.ShowPrev = function() {
  index = (index - 1) % WebViewer.nSlides;
  if (index < 0) index = WebViewer.nSlides - 1;
  WebViewer.setSlide(index);
};
WebViewer.ShowNext = function() {
  index = (index+1) % WebViewer.nSlides;
  WebViewer.setSlide(index);
};
WebViewer.setSlide = function(index) {
  if (WebViewer.nSlides == 0) {
    WebViewer.ShowError("No canvas name selected!");
    return false;
  }
  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) return;
  var url = WebViewer.BASE_URL + WebViewer.slideList[index]; 
  if (WebViewer.IsOnline()) url = WebViewer.addRandom(url);
  canvas.src = url; 
};
WebViewer.ListSelectAll = function(option) {
  var obj = document.getElementById("canvaslistarea");
  if (obj == null) {
    WebViewer.ShowError("List area object null!!");
    return false;
  }
  for (var i = 0; i < obj.length; i++)
    obj.options[i].selected = (option == "select") ? true : false;

  return true;
};
WebViewer.FindItem = function() {
  // Find pattern from the text input
  var obj = document.getElementById("finditem");
  if (obj == null) return false;
  var patt = obj.value;
  if (patt == "") return false;

  // Now find the canvas list area object
  obj = document.getElementById("canvaslistarea");
  if (obj == null) {
    WebViewer.ShowError("List area object null!!");
    return false;
  }
  var len = obj.length;

  for (var i = 0; i < len; i++) {
    var val = obj.options[i].text; // value;
    var loc = val.indexOf(patt);
    if (loc != -1 && i > WebViewer.lastMatchIndex) {
      if (WebViewer.lastMatchIndex == -1) WebViewer.firstMatchIndex = i;
      if (WebViewer.lastMatchIndex > -1)
        obj.options[WebViewer.lastMatchIndex].selected = false;
      obj.options[i].selected = true;
      WebViewer.lastMatchIndex = i;

      break;
    }
    else if (i == len-1) {
      if (WebViewer.lastMatchIndex > -1)
        obj.options[WebViewer.lastMatchIndex].selected = false;
      if (WebViewer.firstMatchIndex > -1)
        obj.options[WebViewer.firstMatchIndex].selected = true;
      WebViewer.lastMatchIndex = -1;
    }
  }
  return true;
};
WebViewer.SetSelectBoxIndex = function(id, name) {
  var obj = document.getElementById(id);
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
WebViewer.EnablePanel = function() {
  var fobj = document.getElementById("fitfunction");
  var robj = document.getElementById("fitrange");
  var eobj = document.getElementById("fitexp");
  var pobj = document.getElementById("fitparams");
  if (fobj == null || robj == null || eobj == null || pobj == null) {
    WebViewer.ShowError('one of the fit panel objects null!');
    return;
  }
  
  if (document.getElementById("enablefit").checked) {
    fobj.disabled = false;
    robj.disabled = false;
    pobj.disabled = false;
    WebViewer.IsUserFunction();
  }
  else {
    fobj.disabled = true;
    robj.disabled = true;
    eobj.disabled = true;
    pobj.disabled = true;
  }
};
WebViewer.IsUserFunction = function() {
  var fobj = document.getElementById("fitfunction");
  var eobj = document.getElementById("fitexp");
  if (fobj == null || eobj == null) return; 

  var value = fobj.options[fobj.selectedIndex].value;
  if (value == "user") {
    eobj.disabled = false;
  }
  else {
    eobj.disabled = true;
  }
};
WebViewer.RunMacro = function() {
  // Get Canavs
  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) {
    WebViewer.ShowError("Canvas obj is not found!");
    return;
  }

  // Macro list
  var obj = document.getElementById("macrolistarea");
  if (obj == null) return;

  var value = obj.options[obj.selectedIndex].value;
  var queryString = "command=macro&run=" + WebViewer.GetRun() + "&name=" + value;
  var url = WebViewer.BASE_URL + escape(queryString); 
  WebViewer.lastURL = url;
  if (WebViewer.IsOnline()) url = WebViewer.addRandom(url);
  canvas.src = url; 
};
WebViewer.ShowPDF = function() {
  if (WebViewer.lastURL == null || WebViewer.lastURL.length <= 0) return; 
  var url = WebViewer.lastURL + '&getpdf=true';
  if (WebViewer.pdfViewerPlugin) url += '&plugin=true';
  // open a new window and then re-direct 
  window.location = url; 
};
WebViewer.RefreshPlot = function() {
  if (!WebViewer.IsOnline()) return;
  if (WebViewer.lastURL == null || WebViewer.lastURL.length <= 0) return; 
  WebViewer.uncheckSlideShow();
    
  // Get Canavs
  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) {
    WebViewer.ShowError("Canvas obj is not found!");
    return;
  }
  canvas.src = WebViewer.addRandom(lastURL); 
};
WebViewer.StartAutoUpdate = function() {
  WebViewer.RefreshPlot();
  WebViewer.autoUpdateTimerID 
     = setTimeout('WebViewer.StartAutoUpdate()', WebViewer.autoUpdateInterval);
};
WebViewer.AutoUpdate = function() {
  if (document.getElementById("autoupdateopt").checked) {
    WebViewer.StartAutoUpdate();
  }
  else {
    WebViewer.StopAutoUpdate(); 
  }
};
WebViewer.CheckPlugin = function() {
  var len = navigator.plugins.length;
  for (var i = 0; i < len; i++) {
    if (WebViewer.DEBUG) 
      WebViewer.ShowError(navigator.plugins[i].name + ", " + navigator.plugins[i].description);
    var name = navigator.plugins[i].name;
    var desc = navigator.plugins[i].description;
    if ((name.indexOf("Adobe Reader") != -1 && desc.indexOf("PDF") != -1) ||
        (name.indexOf("MozPlugger")   != -1 && desc.indexOf("MozPlugger") != -1) )
    {
      WebViewer.pdfViewerPlugin = true;
    }
  }
};
WebViewer.uncheckSlideShow = function() {
  var show = document.getElementById("slideshow");
  if (show != null && show.checked) show.checked = false;
};
WebViewer.RedoCurrentPlots = function() {
  if (!WebViewer.IsOnline()) return;
  WebViewer.uncheckSlideShow();

  WebViewer.RequestPlots();
};
WebViewer.FillSelectBoxItem = function(obj, name, path) {
  var option = new Option(name, path);
  option.title = path;
  try {
    obj.add(option, null);
  }
  catch (e) {
    obj.add(option, -1);
  }
};
WebViewer.FillSelectBoxItems = function(rows, destObj, index) {
  // Run number select box
  var obj = document.getElementById(destObj);
  if (obj == null) {
    WebViewer.ShowError('Run option Object, '+destObj+ ' not found!');
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
WebViewer.PlotAction = function(obj) {
  if (obj == null) return;
  var path = obj.name;
  WebViewer.SetSelectBoxIndex('canvaslistarea', path);
  WebViewer.RequestPlots(path);
};
WebViewer.RequestSinglePlot = function(obj) {
  var index = obj.selectedIndex;
  if (index < 0) return;
  var name  = obj.options[index].value + '/' + obj.options[index].text;
  WebViewer.RequestPlots(name);
};
WebViewer.SetProgress = function(option) {
  var progress = document.getElementById('progressbar');
  if (progress == null) return;
  var label = document.getElementById('datatypeid');
  if (label == null) return;
  var args = arguments[1];
  if (args != null && args.length > 0) {
    label.innerHTML = 'Please wait, loading '+args;
  }
  progress.style.visibility = option; // "visible" or "hidden"
};
WebViewer.randOrd =  function () {
  return (Math.round(Math.random())-0.5); 
};
WebViewer.IsOnline =  function() {
  if (WebViewer.GetRun() == "online") return true;
  return false;
};
WebViewer.GetPath = function(node) {
  var pathList = new Array;
  var parent = node.parentNode;
  while (parent && parent.nodeName != '#document') {
    pathList.push(parent.nodeName);
    parent = parent.parentNode;
  }
  pathList.reverse();
  return pathList.join('/');
};
// Obsolete or obsolescence
WebViewer.FillCanvasList2 = function() {
  if (WebViewer.request.readyState == 4) {
    if (WebViewer.request.status == 200) {
      try {
        var doc = WebViewer.request.responseXML;
        var root = doc.documentElement;
        var rows = root.getElementsByTagName('canvas');
        WebViewer.FillSelectBoxItems(rows, 'canvaslistarea', 0);

        setTimeout('WebViewer.RequestTreeView()', 200);
      }
      catch (err) {
        WebViewer.ShowError("Error detail: " + err.message);
      }
    }
    else {
      WebViewer.ShowError("FillCanvasList2:  ERROR: " + WebViewer.request.readyState 
                                               + ", " + WebViewer.request.status);
    }
  }
};
WebViewer.SetConsumerOnly = function() {
  var dobj = document.getElementById('treeviewid'); // div containing the treeview
  if (dobj == null) return
  if (document.getElementById("consumeronly").checked) {
    dobj.style.visibility = 'hidden';
  }
  else {
    dobj.style.visibility = 'visible';
  }
};
// Utility functions
WebViewer.stopRKey = function(evt) {
  var evt = (evt) ? evt : ((event) ? event : null);
  var node = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
  if ((evt.keyCode == 13) && (node.type == "text")) {return false;}
};
WebViewer.addRandom = function(url) {
  return (url + '&t=' + Math.random());
};
WebViewer.ShowTreeContent = function(elem) {
  var nwin = window.open('','name','height=400,width=500');
  nwin.document.write('<html><head><title>Popup</title>');
  nwin.document.write('</head><body>');
  nwin.document.write(elem.innerHTML);
  nwin.document.write('<p><a href="javascript:self.close()"> Close</a> the popup.</p>');
  nwin.document.write('</body></html>');
  nwin.document.close();
};
WebViewer.addText = function(element, text) {
  var obj = document.getElementById(element);
  if (obj == null) return;
  var d = new Date();
  var m = d.getMonth() + 1;
  var now = d.getFullYear() + '/' + m + '/' + d.getDate() 
    + '-' + d.getHours() +':' + d.getMinutes() +':' + d.getSeconds();
  obj.value += ((obj.value == '') ? '' : "\n") + now + " > " + text;
};
WebViewer.clearText = function(element) {
  var obj = document.getElementById(element);
  if (obj == null) return;
  obj.value = '';
};
WebViewer.ShowError = function(message) {
  WebViewer.addText('errorlogger', message);
  showTab('dhtmlgoodies_tabView3', 1);
};
WebViewer.UncheckBox = function(id) {
  var obj = document.getElementById(id);
  if (obj == null) return false;
  obj.checked = false;
  return true;
};
WebViewer.ShowHideLoggerPanel = function() {
  var obj = document.getElementById('showlogger');
  if (obj == null) return false;
  WebViewer.ShowHidePanel('loggerpanel', (obj.checked) ? 'visible' : 'hidden');
};
WebViewer.ShowHidePanel = function(id, option) {
  var obj = document.getElementById(id);
  if (obj == null) return false;
  obj.style.visibility = option;
  return true;
};
// input field's event handlers 
window.onload = function() {
  document.onkeypress = WebViewer.stopRKey;

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
    WebViewer.UncheckBox(cbList[i]);

  WebViewer.clearText('eventlogger');
  WebViewer.clearText('errorlogger');

  // this takes time 
  WebViewer.RequestRunList();

  // Tackle MSIE
  var agt = navigator.userAgent.toLowerCase();
  if (agt.indexOf('msie') != -1) {
    if (WebViewer.DEBUG) 
      WebViewer.addText('errorlogger', "Does not work that well on IE! I'm working on it. - subir");
    var obj = document.getElementById('divimg');
    if (obj != null) obj.style.width = '55%';

    obj = document.getElementById('drawingcanvas');
    if (obj != null) obj.style.width = '95%';

    obj = document.getElementById('getpdf');
    if (obj != null) obj.style.marginLeft = '83%';

    obj = document.getElementById('finditem');
    if (obj != null) obj.style.width = '50%';
  }
  WebViewer.EnablePanel();
  if (agt.indexOf('msie') == -1) WebViewer.CheckPlugin();
}; 
window.onunload = function() { 
  WebViewer.StopAutoUpdate();
  WebViewer.StopSlideShow();
};
