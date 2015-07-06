var request = null; 
var BASE_URL = "http://cdftest.cnaf.infn.it:8081/cgi-bin/ajax/dev/viewer.exe?";
var lastURL  = "";
var canvasList = new Array();
var slideList = new Array();
var slideShowSpeed = 8000;  // miliseconds
var autoUpdateInterval = 60000;  // miliseconds
var index = 0;
var nSlides = 0;
var timerID;
var autoUpdateTimerID = null;
var MAX_SLIDES = 20;
var lastMatchIndex = -1;
var firstMatchIndex = -1;
var pdfViewerPlugin = false;
var DEBUG = false;

// ------------------------------------------------
// From AJAX Hacks, Bruce W. Perry, O'Reilly, 2005
// -----------------------------------------------
/* Wrapper function for constructing a request object. 
   Parameters: 
     <reqType>: The HTTP request type, such as GET or POST. 
     <url>: The URL of the server program. 
     <asynch>: Whether to send the request asynchronously or not. 
     <respHandle>: The name of the function that will handle the response. 

     Any fifth parameters, represented as arguments[4], are the data a 
     POST request is designed to send. 
*/ 
function httpRequest(reqType, url, asynch, respHandle) { 
  if (window.XMLHttpRequest) {   // Mozilla-based browsers 
    request = new XMLHttpRequest(); 
  } 
  else if (window.ActiveXObject) { 
    request = new ActiveXObject("Msxml2.XMLHTTP"); 
    if (!request) { 
      request = new ActiveXObject("Microsoft.XMLHTTP"); 
    } 
  } 
  // very unlikely, but we test for a null request 
  // if neither ActiveXObject was initialized 
  if (request) { 
    // if the reqType parameter is POST, then the 
    // 5th argument to the function is the POSTed data 
    if (reqType.toLowerCase() != "post") { 
      initReq(reqType, url, asynch, respHandle); 
    }  
    else { 
      // the POSTed data 
      var args = arguments[4]; 
      if (args != null && args.length > 0) { 
         initReq(reqType, url, asynch, respHandle, args); 
      } 
    } 
  } 
  else { 
    alert("Your browser does not permit the use of all "+ 
          "of this application's features!"); 
  } 
} 
// Initialize a request object that is already constructed 
function initReq(reqType, url, bool, respHandle) { 
  try { 
    // Specify the function that will handle the HTTP response 
    request.onreadystatechange = respHandle; 
    request.open(reqType, url, bool); 

    // if the reqType parameter is POST, then the 
    // 5th argument to the function is the POSTed data 
    if (reqType.toLowerCase() == "post") { 
      request.setRequestHeader("Content-Type", 
           "application/x-www-form-urlencoded; charset=UTF-8"); 
      request.send(arguments[4]); 
    }  
    else { 
      request.send(null); 
    } 
  } 
  catch (errv) { 
    alert ( 
        "The application cannot contact " + 
        "the server at the moment. " + 
        "Please try again in a few seconds.\\n" + 
        "Error detail: " + errv.message); 
  } 
}
//input field's event handlers 
window.onload=function() {
  initTabs('dhtmlgoodies_tabView1', Array('List View','Tree View'), 0,'99.5%','100%');
  initTabs('dhtmlgoodies_tabView2', Array('Options','Fitting','Macro'), 0,'99.5%','100%');

  // Tooltip
  var tooltipObj = new DHTMLgoodies_formTooltip();
  tooltipObj.setTooltipPosition('right');
  tooltipObj.setPageBgColor('#EEEEEE');
  tooltipObj.setTooltipCornerSize(15);
  tooltipObj.initFormFieldTooltip();

  // this takes time 
  RequestRunList();

  // Tackle MSIE
  var agt = navigator.userAgent.toLowerCase();
  if (agt.indexOf('msie') != -1) {
    if (DEBUG) 
    alert ("Does not work that well on IE! I'm working on it. - subir");
    var obj = document.getElementById('divimg');
    if (obj != null) obj.style.width = '55%';

    obj = document.getElementById('drawingcanvas');
    if (obj != null) obj.style.width = '95%';

    obj = document.getElementById('getpdf');
    if (obj != null) obj.style.marginLeft = '83%';

    obj = document.getElementById('finditem');
    if (obj != null) obj.style.width = '50%';
  }
  EnablePanel();
  if (agt.indexOf('msie') == -1) CheckPlugin();
} 
window.onunload=function() { 
  StopAutoUpdate();
  StopSlideShow();
}
window.onresize = scale;
function randOrd() {
  return (Math.round(Math.random())-0.5); 
}
function IsOnline() {
  if (GetRun() == "online") return true;
  return false;
}
function GetPath(node) {
  var pathList = new Array;
  var parent = node.parentNode;
  while (parent && parent.nodeName != '#document') {
    pathList.push(parent.nodeName);
    parent = parent.parentNode;
  }
  pathList.reverse();
  //pathList.shift();

  return pathList.join('/');
}
function RequestRunList() {
  var queryString = "command=runlist";
  queryString += '&t='+Math.random();  // important
  var url = BASE_URL+escape(queryString); 
  httpRequest("GET", url, true, FillRunList); 
}
function FillRunList() {
  if (request.readyState == 4) {
    if (request.status == 200) {
      try {
        var response = eval('(' + request.responseText + ')');
        var rows = response.runs;
        FillSelectBoxItems(rows,    'runoption', 0);
        FillSelectBoxItems(rows, 'refrunoption', 1);
      }
      catch (err) {
        alert ("Error detail: " + err.message); 
      }
    } 
    else {
      alert("FillRunList:  ERROR:"+request.readyState+", "+request.status); 
    }
  }
}
function RequestCanvasList() { 
  var obj = document.getElementById("runoption");
  var run = obj.options[obj.selectedIndex].value;
  var queryString = "command=canvaslist&view=xml&run="+run;

  if (document.getElementById("consumeronly").checked)
    queryString += "&consumerlist=true";

  if (IsOnline()) 
    queryString += '&t='+Math.random();
  var url = BASE_URL+escape(queryString); 

  SetProgress('visible');
  //httpRequest("GET", url, true, FillCanvasList2); 
  httpRequest("GET", url, true, FillCanvasList); 
} 
function FillCanvasList() {
  if (request.readyState == 4) {
    if (request.status == 200) {
      try {
        var doc = request.responseXML;
        var root = doc.documentElement;
        FillList(root);
        SetProgress('hidden');
      }
      catch (err) {
        alert ("Error detail: " + err.message); 
      }
    } 
    else {
      alert("FillCanvasList:  ERROR:"+request.readyState+", "+request.status); 
    }
  }
}
function FillCanvasList2() {
  if (request.readyState == 4) {
    if (request.status == 200) {
      try {
        var doc = request.responseXML;
        var root = doc.documentElement;
        var rows = root.getElementsByTagName('canvas');
        FillSelectBoxItems(rows, 'canvaslistarea', 0);

        setTimeout('RequestTreeView()', 200);
      }
      catch (err) {
        alert ("Error detail: " + err.message);
      }
    }
    else {
      alert("FillCanvasList2:  ERROR:"+request.readyState+", "+request.status);
    }
  }
}
function RequestTreeView() { 
  var obj = document.getElementById("runoption");
  var run = obj.options[obj.selectedIndex].value;
  var queryString = "command=canvaslist&view=tree&run="+run;

  if (IsOnline()) 
    queryString += '&t='+Math.random();
  var url = BASE_URL+escape(queryString); 

  httpRequest("GET", url, true, FillTreeView); 
}
function FillTreeView() {
  if (request.readyState == 4) {
    if (request.status == 200) {
      try {
        var data = request.responseText;
        document.getElementById('treeviewid').innerHTML = data;
        initTree();
        SetProgress('hidden');
      }
      catch (err) {
        alert ("Error detail: " + err.message); 
      }
    } 
    else {
      alert("FillTreeView: ERROR:"+request.readyState+", "+request.status); 
    }
  }
}
function GetRun() {
  var obj = document.getElementById("runoption");
  var value = obj.options[obj.selectedIndex].value;

  return value;
}
function GetSelectedCanvasList() {
  var obj = document.getElementById("canvaslistarea");
  var len = obj.length;
  if (len == 0) {
    if (DEBUG) alert("Canvas List Area empty!");
    return false;
  }
  
  var k = 0;
  for (var i = 0; i < len; i++) {
    if (obj.options[i].selected) {
      var val = obj.options[i].value + '/' + obj.options[i].text;
      canvasList[k] = val;
      k++;
    }
  }
  nSlides = k;
  if (nSlides == 0) {
    if (DEBUG) alert("No canvas name selected!");
    return false;
  }

  return true;
}
function StopAutoUpdate() {
  if (IsOnline() && autoUpdateTimerID != null) {
    clearTimeout(autoUpdateTimerID); 
  }
}
function GetCanvasProperties () {
  // Get Canavs properties
  var text = '';
  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) {
    alert("Canvas obj is not found!");
    return text;
  }
  text = '&width='+canvas.width+'&height='+canvas.height;
  return text;
}
function RunSlideShow() {
  // We're using GET method
  var run = GetRun();
  var queryString = 'command=plot&run='+run;
  queryString += GetCanvasProperties();

  if (GetSelectedCanvasList() == false) return;
  if (nSlides > 1) {
    for (var i = 0; i < canvasList.length; i++)
      slideList[k] = queryString + '&canvas='+escape(canvasList[i]);
    StartSlideShow();  
  }
}
function RequestPlots() { 
  var plot = arguments[0];
  if (plot == null) plot = '';
  // Check if it is a slideshow
  if (document.getElementById('slideshow').checked) {
    RunSlideShow();
  }
  else {
    // EXPERIMENTAL - applies only to online histogram browsing
    // A new plot has been requested, so uninstall autoupdate
    if (IsOnline()) StopAutoUpdate();

    // We're using GET method
    var run = GetRun();
    var queryString = 'command=plot&run='+run;
    queryString += GetCanvasProperties();
   
    // Collect canvas names
    if (plot.length == 0) {
      var retval = GetSelectedCanvasList();
      if (retval == false) return;
    }

    // Make sure that too many slides are not requested by mistake
    if (nSlides > MAX_SLIDES) {
      alert("Too many slides selected!, nSlide="+nSlides+" reasonable only for slideshow");
      return;
    }

    // Form canvas list
    if (plot.length != 0) {
      queryString += '&canvas='+plot;
      nSlides = 1;
    }
    else {
      for (var i = 0; i < nSlides; i++) 
        queryString += '&canvas=' + canvasList[i];
    }

    // logy option
    if (document.getElementById('logy').checked) queryString += '&logy=true';
   
    // Is histogram Fitting requested? 
    if (document.getElementById('enablefit').checked) {
      queryString += '&reqfit=true';
   
      // Fit method
      var obj = document.getElementById('fitfunction');
      if (obj == null) {
        alert("Fit Function option object not found!");
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
    if (nSlides > 1 ) {
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
      queryString += '&rows' + r + '&cols=' + c;
    }
    if (nSlides == 1 || document.getElementById("forcexrange").checked) {
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
   
    if (IsOnline()) queryString += '&t='+Math.random();
    var url = BASE_URL+escape(queryString);
    lastURL = url;

    var canvas = document.getElementById("drawingcanvas");
    if (canvas == null) {
      alert("Canvas obj is not found!");
      return;
    }
    canvas.src = url; 
   
    // EXPERIMENTAL
    // reinstall autoupdate
    if (IsOnline()) {
      var obj = document.getElementById("autoupdateopt");
      if (obj != null && !obj.checked) obj.checked = true;
      StartAutoUpdate();
    }
  }
} 
function StartSlideShow() {
  setSlide(index);
  index = (index+1) % nSlides;
  timerID = setTimeout('StartSlideShow()', slideShowSpeed);
} 
function StopSlideShow() {
  if (timerID != null) clearTimeout(timerID); 
}
function ShowFirst() {
  setSlide(0);
}
function ShowLast() {
  setSlide(nSlides-1);
}
function ShowPrev() {
  index = (index-1) % nSlides;
  if (index<0) index = nSlides-1;
  setSlide(index);
}
function ShowNext() {
  index = (index+1) % nSlides;
  setSlide(index);
}
function setSlide(index) {
  if (nSlides == 0) {
    if (DEBUG) alert("No canvas name selected!");
    return false;
  }
  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) return;
  var url = BASE_URL + slideList[index]; 
  if (IsOnline()) url += '&t='+Math.random();
  canvas.src = url; 
}
function ListSelectAll(option) {
  var obj = document.getElementById("canvaslistarea");
  if (obj == null) {
    if (DEBUG) alert("List area object null!!");
    return false;
  }
  for (var i = 0; i < obj.length; i++) {
    obj.options[i].selected = (option == "select") ? true : false;
  }
  return true;
}
function FindItem() {
  // Find pattern from the text input
  var obj = document.getElementById("finditem");
  if (obj == null) return false;
  var patt = obj.value;
  if (patt == "") return false;

  // Now find the canvas list area object
  obj = document.getElementById("canvaslistarea");
  if (obj == null) {
    if (DEBUG) alert("List area object null!!");
    return false;
  }
  var len = obj.length;

  for (var i = 0; i < len; i++) {
    var val = obj.options[i].text; // value;
    var loc = val.indexOf(patt);
    //alert("loc="+loc+" i="+i+" last="+lastMatchIndex+" first="+firstMatchIndex);
    if (loc != -1 && i > lastMatchIndex) {
      if (lastMatchIndex == -1) firstMatchIndex = i;
      if (lastMatchIndex > -1)
        obj.options[lastMatchIndex].selected = false;
      obj.options[i].selected = true;
      lastMatchIndex = i;

      break;
    }
    else if (i == len-1) {
      if (lastMatchIndex > -1)
        obj.options[lastMatchIndex].selected = false;
      if (firstMatchIndex > -1)
        obj.options[firstMatchIndex].selected = true;
      lastMatchIndex = -1;
    }
  }
  return true;
}
function EnablePanel() {
  var fobj = document.getElementById("fitfunction");
  var robj = document.getElementById("fitrange");
  var eobj = document.getElementById("fitexp");
  var pobj = document.getElementById("fitparams");
  if (fobj == null || robj == null || eobj == null || pobj == null) {
    alert('one of the fit panel objects null!');
    return;
  }
  
  if (document.getElementById("enablefit").checked) {
    fobj.disabled = false;
    robj.disabled = false;
    pobj.disabled = false;
    IsUserFunction();
  }
  else {
    fobj.disabled = true;
    robj.disabled = true;
    eobj.disabled = true;
    pobj.disabled = true;
  }
}
function IsUserFunction() {
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
}
function RunMacro() {
  // Get Canavs
  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) {
    alert("Canvas obj is not found!");
    return;
  }

  // Macro list
  var obj = document.getElementById("macrolistarea");
  if (obj == null) return;

  var value = obj.options[obj.selectedIndex].value;
  var queryString = "command=macro&run="+GetRun()+"&name="+value;
  var url = BASE_URL+escape(queryString); 
  lastURL = url;
  canvas.src = url; 
}
function ShowPDF() {
  if (lastURL == null || lastURL.length <= 0) return; 
  url = lastURL+'&getpdf=true';
  if (pdfViewerPlugin) url += '&plugin=true';
  window.location = url; 
}
function RefreshPlot() {
  if (!IsOnline()) return;
  if (lastURL == null || lastURL.length <= 0) return; 
  uncheckSlideShow();
    
  var url = lastURL+'&t='+Math.random();
  // Get Canavs
  var canvas = document.getElementById("drawingcanvas");
  if (canvas == null) {
    alert("Canvas obj is not found!");
    return;
  }
  canvas.src = url; 
}
function StartAutoUpdate() {
  RefreshPlot();
  autoUpdateTimerID = setTimeout('StartAutoUpdate()', autoUpdateInterval);
}
function AutoUpdate() {
  if (document.getElementById("autoupdateopt").checked) {
    StartAutoUpdate();
  }
  else {
    StopAutoUpdate(); 
  }
}
function CheckPlugin() {
  var len = navigator.plugins.length;
  for (var i = 0; i < len; i++) {
    if (DEBUG) 
      alert(navigator.plugins[i].name + ", " + navigator.plugins[i].description)
    var name = navigator.plugins[i].name;
    var desc = navigator.plugins[i].description;
    if ((name.indexOf("Adobe Reader") != -1 && desc.indexOf("PDF") != -1) ||
        (name.indexOf("MozPlugger")   != -1 && desc.indexOf("MozPlugger") != -1) )
    {
      pdfViewerPlugin = true;
    }
  }
}
function uncheckSlideShow() {
  var show = document.getElementById("slideshow");
  if (show != null && show.checked) show.checked = false;
}
function RedoCurrentPlots() {
  if (!IsOnline()) return;
  uncheckSlideShow();

  RequestPlots();
}
function FillSelectBoxItem(obj, name, path) {
  var option = new Option(name, path);
  try {
    obj.add(option, null);
  }
  catch (e) {
    obj.add(option, -1);
  }
}
function FillSelectBoxItems(rows, destObj, index) {
  // Run number select box
  var obj = document.getElementById(destObj);
  if (obj == null) {
    alert('Run option Object, '+destObj+ ' not found!');
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
}
function hasChildNode(node) {
  for (var i = 0; i < node.childNodes.length; i++) {
    var obj = node.childNodes.item(i);
    if (obj.nodeType != 1) continue;
    return true;
  }
  return false;
}
function FillList(root) {
  // Run number select box
  var listview = document.getElementById('canvaslistarea');
  if (listview == null) {
    alert('Canvas list Object, not found!');
    return;
  }
  listview.options.length = 0;

  var theUL = document.createElement('UL');
  theUL.setAttribute('id',"dhtmlgoodies_tree");
  theUL.setAttribute('class',"dhtmlgoodies_tree");

  traverse(root, theUL, listview);

  var div = document.getElementById('treeviewid');
  if (div == null) return;
  div.removeChild(div.firstChild); 
  div.appendChild(theUL);

  initTree();
}
function traverse(node, element, listview) {
  for (var i = 0; i < node.childNodes.length; i++) {
    var obj = node.childNodes.item(i);
    if (obj.nodeType != 1) continue;

    var aLi = document.createElement('LI');
    var aTag = document.createElement('A');
    aTag.setAttribute('href',"#");
    var text; 
    if (obj.nodeName == 'canvas') {
      var name = obj.textContent;
      var path = GetPath(obj);
      text = document.createTextNode(name);
      aTag.setAttribute('name', path+'/'+name);
      aTag.setAttribute('onclick',"PlotAction(this)");

      FillSelectBoxItem(listview, name, path);       
    }
    else {
      text = document.createTextNode(obj.nodeName);
    }
    aTag.appendChild(text);
    aLi.appendChild(aTag);
    if (hasChildNode(obj) && obj.nodeName != 'canvas') {
      aLi.setAttribute('class',"dir");
      var aUl = document.createElement('UL');
      traverse(obj, aUl, listview);
      aLi.appendChild(aUl);
    }
    element.appendChild(aLi);
  }  
}
function PlotAction(obj) {
  RequestPlots(obj.name);
}
function RequestSinglePlot(obj) {
  var index = obj.selectedIndex;
  if (index < 0) return;
  var name  = obj.options[index].value + '/' + obj.options[index].text;
  RequestPlots(name);
}
function SetProgress (option) {
  var progress = document.getElementById('progress');
  if (progress == null) return;
  progress.style.visibility = option; // "visible" or "hidden"
}
