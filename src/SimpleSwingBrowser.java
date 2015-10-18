import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker.State;
import org.w3c.dom.*; 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.*;
import javax.tools.*;
import netscape.javascript.JSObject;
import java.nio.channels.*;
import javax.activation.MimeType;
import static javafx.concurrent.Worker.State.FAILED;
  
public class SimpleSwingBrowser extends JFrame {
    public static final int ProjectUTW_VERSION = 14;
    private static int latestVersion = -1;
 
    private final JFXPanel jfxPanel = new JFXPanel();
    private static WebEngine engine;
    
    private static String[] proxy = new String[2];
 
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JPanel tabs = new JPanel(new BorderLayout());
    private final JLabel lblStatus = new JLabel();
    
    private JButton newTab = new JButton("New tab");
    private JButton newWindow = new JButton("New window");

    private final JButton btnGo = new JButton("Go");
    private JToolBar toolbar = new JToolBar();
    private static final PlaceholderTextField txtURL = new PlaceholderTextField();
    private final JProgressBar progressBar = new JProgressBar();
    private static Font toolbarFont = new Font("Calibri", Font.BOLD, 13);
    
    private static SimpleSwingBrowser browser;
    
    private static ArrayList<String> execJS = new ArrayList<String>();
    
    private static WebExtensions webExtensions = null;
    
    public static final String PATH = getProgramPath();
    private static ArrayList<String> pluginCode = new ArrayList<String>();
    private static String _url;
    private static final HashMap<String, String> SEARCH_ENGINES = new HashMap<String, String>();
    private static String SEARCH_ENGINE;
    private static final String HOMEPAGE = "get: Welcome";
 
    public SimpleSwingBrowser() {
        super();
        initComponents();
    }
    private static String readFile(String filename) throws IOException
    {
        String content = null;
        File file = new File(filename); //for ex foo.txt
        FileReader reader = null;
        try{
            reader = new FileReader(file);
        } catch(FileNotFoundException e){
            return "";
        }
        char[] chars = new char[(int) file.length()];
        reader.read(chars);
        content = new String(chars);
        reader.close();
        return content;
    }
    private static void loadPlugins(String path) throws IOException{
        String[] plugins = null;
        plugins = readFile(path + File.separator + "plugins.txt").split("<Plugin type=\"text/javascript\">");
        for(int x=0; x<plugins.length; x++){
            pluginCode.add(plugins[x]);
        }
    }
    private static void loadPlugins() throws IOException{
        loadPlugins(PATH.substring(0, PATH.lastIndexOf(File.separator)));
    }
    private static String getProgramPath(){
        String path = null;
        try{
            path = new File(SimpleSwingBrowser.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch(URISyntaxException e){
            // Ignore
        }
        return path;
    }
    public void setCloseProcedure(){
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
                System.exit(0);
            }
        });
    }

    private static boolean isRedirect(int statusCode) {
        if (statusCode != HttpURLConnection.HTTP_OK) {
            if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                || statusCode == HttpURLConnection.HTTP_MOVED_PERM
                    || statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
                return true;
            }
        }
        return false;
    }
    
    private void initComponents() {
        setCloseProcedure();
        createScene();
        
 
        ActionListener al = new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                loadURL(txtURL.getText());
            }
        };
        ActionListener newtab = new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                loadURL(txtURL.getText());
            }
        };
        ActionListener newwindow = new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent e) {
                try{
                    startApplication();
                } catch(Throwable t){
                    // Ignore
                }
            }
        };
        
        newWindow.addActionListener(newwindow);
        btnGo.addActionListener(al);
        
        btnGo.setFont(new Font("Calibri", Font.BOLD, 14));
        txtURL.addActionListener(al);
        txtURL.setDisabledTextColor(Color.GRAY);
        txtURL.setFont(new Font("Calibri", Font.BOLD, 18));
  
        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);
  
        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);
        
        toolbar.add(newWindow);
        newWindow.setFont(toolbarFont);
        //toolbar.add(newTab);
        newTab.setFont(toolbarFont);
 
        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 5, 2));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);
 
        topBar.add(toolbar, BorderLayout.SOUTH);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);
        
        Container c = getContentPane();
        c.add(panel);
        
        setPreferredSize(new Dimension(1024, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        pack();
        

    }
 
    private void createScene() {
 
        Platform.runLater(new Runnable() {
            @Override 
            public void run() {
 
                WebView view = new WebView();
                engine = view.getEngine();
                
                webExtensions = new WebExtensions(engine);
 
                engine.titleProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override 
                            public void run() {
                                SimpleSwingBrowser.this.setTitle(newValue);
                            }
                        });
                    }
                });
 
                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override 
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override 
                            public void run() {
                                lblStatus.setText(event.getData());
                            }
                        });
                    }
                });
 
                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override 
                            public void run() {
                                txtURL.setText(newValue);
                                lblStatus.setText("Loading "+newValue);
                            }
                        });
                    }
                });
                
                engine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<State>() {
                        @Override
                        public void changed(ObservableValue<? extends State> ov,
                            State oldState, State newState) {
                            if (newState == State.SUCCEEDED) {
                                // A page loaded successfully
                                engine.executeScript("ProjectUTW_SITE_URL = \"" + _url.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace("'", "\\'") + "\";");
                                for(int x=0; x<pluginCode.size(); x++){
                                    engine.executeScript(pluginCode.get(x));
                                }
                                String html = (String) engine.executeScript("document.body.outerHTML");
                                engine.executeScript("document.body.outerHTML = \"" + parseUTWScript(html).replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace("'", "\\'") + "\";");
                                for(int x=0; x<execJS.size(); x++){
                                    engine.executeScript(execJS.get(x));
                                }
                                org.w3c.dom.events.EventListener listener = new org.w3c.dom.events.EventListener() {
                                    public void handleEvent(org.w3c.dom.events.Event ev) {
                                        String href = ((Element)ev.getTarget()).getAttribute("abs:href");
                                        loadURL(href);
                                    }
                                };
                                NodeList nodeList = engine.getDocument().getElementsByTagName("a");
                                for (int i=0; i<nodeList.getLength(); i++){
                                    ((org.w3c.dom.events.EventTarget)nodeList.item(i)).addEventListener("click", listener, false);
                                }
                                lblStatus.setText("Loaded page.");
                            }
                        }
                    }
                );
 
                engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override 
                            public void run() {
                                progressBar.setValue(newValue.intValue());
                            }
                        });
                    }
                });

                engine.getLoadWorker()
                        .exceptionProperty()
                        .addListener(new ChangeListener<Throwable>() {
 
                            public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                                if (engine.getLoadWorker().getState() == FAILED) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override public void run() {
                                            JOptionPane.showMessageDialog(
                                                    panel,
                                                    (value != null) ?
                                                    engine.getLocation() + "\n" + value.getMessage() :
                                                    engine.getLocation() + "\nUnexpected error.",
                                                    "Loading error...",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                                } else{
                                    // Do nothing as of yet
                                }
                            }
                        });

                jfxPanel.setScene(new Scene(view));
            }
        });
    }
 
    public void loadURL(final String url) {
        _url = url;
        final String BEFORE_URL = txtURL.getText();
        Platform.runLater(new Runnable() {
            @Override 
            public void run() {
                if(url.startsWith("proxy:")){
                    setProxy(url.split(">")[0].substring(6).trim());
                    String url2;
                    try{
                        url2 = url.split(">")[1].trim();
                    } catch(java.lang.IndexOutOfBoundsException e){
                        url2 = "http://www.google.com/ncr";
                    }
                    loadURL(url2);
                } else if(url.startsWith("get:")){
                    getAttrib(url.substring(4).trim());
                } else{
                    if(url.startsWith("search:")){
                        String eng = url.substring(7).trim().toLowerCase();
                        if(SEARCH_ENGINES.containsKey(eng)){
                            SEARCH_ENGINE = SEARCH_ENGINES.get(eng);
                            txtURL.setText(BEFORE_URL);
                        } else{
                            if(eng.contains("%s")){
                                SEARCH_ENGINE = eng;
                                txtURL.setText(BEFORE_URL);
                            }
                        }
                    }else if(url.startsWith("javascript:")){
                        engine.executeScript(url.substring(11));
                        txtURL.setText(BEFORE_URL);
                    } else if(url.startsWith("mailto:")){
                        Desktop desktop;
                        URI mailto = null;
                            if (Desktop.isDesktopSupported() 
                                && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
                              try{
                                  mailto = new URI(_url);
                                  desktop.mail(mailto);
                              } catch(IOException e){
                                  // Ignore
                              } catch(URISyntaxException e){
                                  // Ignore
                              }
                            }
                        txtURL.setText(BEFORE_URL);
                    } else if((url.contains(" ") || url.contains("\"") || url.contains("'") || (!url.contains(".") && !url.equals("localhost"))) && !url.trim().startsWith("!")){
                        if(url.trim().startsWith("~")){
                            _url = _url.trim().substring(1);
                        }
                        try{
                            loadURL(SEARCH_ENGINE.replace("%s", URLEncoder.encode(_url, "UTF-8")));
                        } catch(IOException e){
                            // Ignore
                        }
                    } else{
                        if(url.trim().startsWith("!")){
                            _url = _url.trim().substring(1);
                        } else if(url.trim().startsWith("~")){
                            _url = _url.trim().substring(1);
                            try{
                                _url = SEARCH_ENGINE.replace("%s", URLEncoder.encode(_url, "UTF-8"));
                            } catch(IOException e){
                                // Ignore
                            }
                        }
                        String tmp = toURL(_url.trim());
         
                        if (tmp == null) {
                            tmp = toURL("http://" + _url);
                        }
         
                        engine.load(tmp);
                    }
                }
            }
        });
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
                return null;
        }
    }

    private void getAttrib(String attrib){
        if(attrib.toLowerCase().equals("proxy")){
            if(proxy[0].equals("") && proxy[1].equals("")){
                JOptionPane.showMessageDialog(null, "You are not using a proxy.");
            } else{
                JOptionPane.showMessageDialog(null, "You are currently using the HTTP proxy "+proxy[0]+":"+proxy[1]);
            }
        } else if(attrib.toLowerCase().equals("ip")){
            try{
                JOptionPane.showMessageDialog(null, "Your current external IP is "+getIp());
            } catch(IOException e){
                // Do nothing.
            }
        } else if(attrib.toLowerCase().equals("version")){
            engine.loadContent("<html><body>Current version: "+ProjectUTW_VERSION+"\n<br>\nLatest version: "+latestVersion+"\n</body></html>");
        } else if(attrib.toLowerCase().equals("web extensions")){
            webExtensions = new WebExtensions(engine);
        } else if(attrib.toLowerCase().equals("remove web extensions")){
            webExtensions = null;
        } else if(attrib.toLowerCase().equals("real ip")){
            getStunIP();
        } else if(attrib.toLowerCase().equals("javascript")){
            getJSSupport();
        } else if(attrib.toLowerCase().equals("iframes")){
            getIframeSupport();
        } else if(attrib.toLowerCase().equals("project")){
            browser.loadURL("http://crash0verrid3.github.io/Project-UTW/");
        } else if(attrib.toLowerCase().equals("welcome")){
            engine.loadContent("<!DOCTYPE html>\n<html>\n<head>\n<title>Project UTW</title>\n</head>\n<body>\n<h1>Welcome to the Project UTW browser</h1>\n\n<p><a href=\"get: project\">View Project UTW on Github</a></p>\n<p>Project UTW is an open-source browser written by <strong>Alex Anderson</strong> using only the Java programming language.</p>\n<p>For a tutorial on using this browser, just <a href=\"get: tutorial\">click here</a>.<strong><em><br /></em></strong></p>\n<p>This browser will never keep any permanant history from your browsing,</p>\n<p>and is designed for easy use with a <em><a href=\"https://en.wikipedia.org/wiki/Proxy_server\">web proxy</a></em>.</p>\n<p>&nbsp;</p>\n<p>At any time, you can type into the URL bar \"proxy: [proxy ip:port goes here]\"</p>\n<p>and the browser will use that proxy. Note, the proxy will not be saved for use</p>\n<p>after you close the browser. You can also type instead of the ip:port of the proxy:</p>\n<ul>\n<li>\"none\" - Restores the browser to not using a proxy</li>\n<li>\"default\" - Uses a preconfigured proxy server.</li>\n</ul>\n<p>To get the current proxy, type \"get: proxy\" into the URL bar.</p>\n</body>\n</html>\n");
        } else if(attrib.toLowerCase().equals("tutorial")){
            engine.loadContent("<!DOCTYPE html>\n<html>\n<head>\n</head>\n<body>\n<h1>Welcome to the Project UTW browser tutorial</h1>\n<p>Project UTW is an open-source browser written by <strong>Alex Anderson</strong> using only the Java programming language.</p>\n<p>This browser will never keep any permanant history from your browsing,</p>\n<p>and is designed for easy use with a <em><a href=\"https://en.wikipedia.org/wiki/Proxy_server#Types_of_proxy\">web proxy.</a></em></p>\n<p>&nbsp;</p>\n<p><em>To go back to the previous page or go forward a page, right-click the current page and click the</em></p>\n<p><em>corresponding option.</em></p>\n<p>&nbsp;</p>\n<p>At any time, you can type into the URL bar \"proxy: [proxy ip:port goes here]\"</p>\n<p>and the browser will use that proxy. Note, the proxy will not be saved for use</p>\n<p>after you close the browser. You can also type instead of the ip:port of the proxy:</p>\n<ul>\n<li>\"none\" - Restores the browser to not using a proxy</li>\n<li>\"default\" - Uses a preconfigured proxy server.</li>\n</ul>\n<p>To go to a website automatically after the proxy is set, type \"proxy: [proxy goes here] &gt; [website to go to]\".</p>\n<p>To get the current proxy, type \"get: proxy\" into the URL bar.</p>\n<p>When you type \"get: [whatever]\", it is called a <em>browser command</em>.&nbsp; Current browser</p>\n<p>commands include:</p>\n<ul>\n<li>\"proxy\" - Shows a popup telling you the currently used proxy server</li>\n<li>\"ip\" - Shows the IP that other websites see when you connect</li>\n<li>\"real ip\" - Shows you the IP websites will see when making STUN requests.<br />There should not be an actual IP shown. If there is no IP shown, you are safe<br />from websites peeking past your proxy.</li>\n<li>\"javascript\" - Tells you whether the browser supports javascript.</li>\n<li>\"iframes\" - Tells you whether the browser supports inline HTML frames</li>\n<li>\"welcome\" - Shows you the page you see when you first open the browser</li>\n<li>\"tutorial\" - Shows you this page</li>\n</ul>\n<p>&nbsp;</p>\n</body>\n</html>\n");
        } else{
            JOptionPane.showMessageDialog(null, "Error: Could not find '"+attrib+"'.");
        }
    }
    
   private static void setProxy(String host, String port){
       System.setProperty("http.proxyHost", host);
       System.setProperty("http.proxyPort", port);
       proxy[0] = host;
       proxy[1] = port;
    }
    
   private static void setProxy(String host){
                System.setProperty("java.net.useSystemProxies", "false");
                System.setProperty("http.proxySet", "true");
       try{
            System.setProperty("java.net.useSystemProxies", "false");
            System.setProperty("http.proxySet", "true");
            String[] h2 = host.split(":");
            String s1 = h2[0].trim();
            String s2 = h2[1].trim();
            setProxy(s1, s2);
            JOptionPane.showMessageDialog(null, "You are now using the HTTP proxy "+s1+":"+s2);
        } catch(java.lang.IndexOutOfBoundsException e){
            if(host.toLowerCase().equals("none")){
                System.setProperty("http.proxySet", "false");
                System.setProperty("java.net.useSystemProxies", "false");
                setProxy("", "");
                JOptionPane.showMessageDialog(null, "You are not using any proxy.");
            }else if(host.toLowerCase().equals("default")){
                System.setProperty("java.net.useSystemProxies", "false");
                System.setProperty("http.proxySet", "true");
                String h = "46.216.1.99";
                String p = "3128";
                setProxy(h, p);
                JOptionPane.showMessageDialog(null, "You are using the HTTP proxy "+h+":"+p);
            } else if(host.toLowerCase().equals("system")){
                System.setProperty("http.proxySet", "false");
                System.setProperty("java.net.useSystemProxies", "true");
            } else{
                JOptionPane.showMessageDialog(null, "The proxy you entered was invalid!");
            }
        }
    }
    
    private static void setProxy(String host, boolean showMessages){
       System.setProperty("java.net.useSystemProxies", "false");
       System.setProperty("http.proxySet", "true");
       try{
            System.setProperty("java.net.useSystemProxies", "false");
            System.setProperty("http.proxySet", "true");
            String[] h2 = host.split(":");
            String s1 = h2[0].trim();
            String s2 = h2[1].trim();
            setProxy(s1, s2);
            if(showMessages){
                JOptionPane.showMessageDialog(null, "You are now using the HTTP proxy "+s1+":"+s2);
            }
        } catch(java.lang.IndexOutOfBoundsException e){
            if(host.toLowerCase().equals("none")){
                System.setProperty("http.proxySet", "false");
                System.setProperty("java.net.useSystemProxies", "false");
                setProxy("", "");
                if(showMessages){
                    JOptionPane.showMessageDialog(null, "You are not using any proxy.");
                }
            }else if(host.toLowerCase().equals("default")){
                System.setProperty("java.net.useSystemProxies", "false");
                System.setProperty("http.proxySet", "true");
                String h = "46.216.1.99";
                String p = "3128";
                setProxy(h, p);
                if(showMessages){
                    JOptionPane.showMessageDialog(null, "You are using the HTTP proxy "+h+":"+p);
                }
            } else if(host.toLowerCase().equals("system")){
                System.setProperty("http.proxySet", "false");
                System.setProperty("java.net.useSystemProxies", "true");
            } else{
                if(showMessages){
                    JOptionPane.showMessageDialog(null, "The proxy you entered was invalid!");
                }
            }
        }
    }
    
    private static String getIp() throws IOException{
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
        
        String ip = in.readLine(); //you get the IP as a String
        return ip;
    }
    
    private void getStunIP(){
        engine.loadContent("<!DOCTYPE html>\n<html>\n    <head>\n        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n    </head>\n    <body>\n        <h4>\n            Demo for:\n            <a href=\"https://github.com/diafygi/webrtc-ips\">\n                https://github.com/diafygi/webrtc-ips\n            </a>\n        </h4>\n        <p>\n            This demo secretly makes requests to STUN servers that can log your\n            request. These requests do not show up in developer consoles and\n            cannot be blocked by browser plugins (AdBlock, Ghostery, etc.).\n        </p>\n        <h4>Your local IP addresses:</h4>\n        <ul></ul>\n        <h4>Your public IP addresses:</h4>\n        <ul></ul>\n        <h4>Your IPv6 addresses:</h4>\n        <ul></ul>\n        <iframe id=\"iframe\" sandbox=\"allow-same-origin\" style=\"display: none\"></iframe>\n        <script>\n            //get the IP addresses associated with an account\n            function getIPs(callback){\n                var ip_dups = {};\n\n                //compatibility for firefox and chrome\n                var RTCPeerConnection = window.RTCPeerConnection\n                    || window.mozRTCPeerConnection\n                    || window.webkitRTCPeerConnection;\n                var useWebKit = !!window.webkitRTCPeerConnection;\n\n                //bypass naive webrtc blocking using an iframe\n                if(!RTCPeerConnection){\n                    //NOTE: you need to have an iframe in the page right above the script tag\n                    //\n                    //<iframe id=\"iframe\" sandbox=\"allow-same-origin\" style=\"display: none\"></iframe>\n                    //<script>...getIPs called in here...\n                    //\n                    var win = iframe.contentWindow;\n                    RTCPeerConnection = win.RTCPeerConnection\n                        || win.mozRTCPeerConnection\n                        || win.webkitRTCPeerConnection;\n                    useWebKit = !!win.webkitRTCPeerConnection;\n                }\n\n                //minimal requirements for data connection\n                var mediaConstraints = {\n                    optional: [{RtpDataChannels: true}]\n                };\n\n                var servers = {iceServers: [{urls: \"stun:stun.services.mozilla.com\"}]};\n\n                //construct a new RTCPeerConnection\n                var pc = new RTCPeerConnection(servers, mediaConstraints);\n\n                function handleCandidate(candidate){\n                    //match just the IP address\n                    var ip_regex = /([0-9]{1,3}(\\.[0-9]{1,3}){3}|[a-f0-9]{1,4}(:[a-f0-9]{1,4}){7})/\n                    var ip_addr = ip_regex.exec(candidate)[1];\n\n                    //remove duplicates\n                    if(ip_dups[ip_addr] === undefined)\n                        callback(ip_addr);\n\n                    ip_dups[ip_addr] = true;\n                }\n\n                //listen for candidate events\n                pc.onicecandidate = function(ice){\n\n                    //skip non-candidate events\n                    if(ice.candidate)\n                        handleCandidate(ice.candidate.candidate);\n                };\n\n                //create a bogus data channel\n                pc.createDataChannel(\"\");\n\n                //create an offer sdp\n                pc.createOffer(function(result){\n\n                    //trigger the stun server request\n                    pc.setLocalDescription(result, function(){}, function(){});\n\n                }, function(){});\n\n                //wait for a while to let everything done\n                setTimeout(function(){\n                    //read candidate info from local description\n                    var lines = pc.localDescription.sdp.split('\n');\n\n                    lines.forEach(function(line){\n                        if(line.indexOf('a=candidate:') === 0)\n                            handleCandidate(line);\n                    });\n                }, 1000);\n            }\n\n            //insert IP addresses into the page\n            getIPs(function(ip){\n                var li = document.createElement(\"li\");\n                li.textContent = ip;\n\n                //local IPs\n                if (ip.match(/^(192\\.168\\.|169\\.254\\.|10\\.|172\\.(1[6-9]|2\\d|3[01]))/))\n                    document.getElementsByTagName(\"ul\")[0].appendChild(li);\n\n                //IPv6 addresses\n                else if (ip.match(/^[a-f0-9]{1,4}(:[a-f0-9]{1,4}){7}$/))\n                    document.getElementsByTagName(\"ul\")[2].appendChild(li);\n\n                //assume the rest are public IPs\n                else\n                    document.getElementsByTagName(\"ul\")[1].appendChild(li);\n            });\n        </script>\n    </body>\n</html>\n");
    }
    private void getJSSupport(){
        engine.loadContent("<!DOCTYPE html>\n<html>\n    <body>\n        Your browser does NOT support javascript.\n    </body>\n    <footer>\n        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n        <script>\n                alert(\"Your browser supports javascript!\");\n                document.body.innerHTML = \"Your browser DOES support javascript!\"\n        </script>\n    </footer>\n</html>\n");
    }
    private void getIframeSupport(){
        engine.loadContent("<!DOCTYPE html>\n<html>\n    <body>\n        <iframe style=\"width: 100%; height: 100%; border: none;\" srcdoc = \"Your browser supports iframes\">Your browser does NOT support iframes.</iframe>\n    </body>\n</html>\n");
    }
    public static boolean option(String message){
        int dialogResult = JOptionPane.showConfirmDialog (null, message);
        return (dialogResult == JOptionPane.YES_OPTION);
    }
    public static ArrayList<String[]> regex(String pattern, String searchIn)
    {
        ArrayList<String[]> out = new ArrayList<String[]>();
        String[] tmp = new String[2];
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(searchIn);
        while(m.find())
        {
            tmp[0] = m.group(1);
            tmp[1] = m.group(2);
            out.add(tmp);
        }
        return out;
    }
    public static boolean contains(String[] values, String regex){
        ArrayList<String[]> out = new ArrayList<String[]>();
        String[] tmp = new String[2];
        Pattern p = Pattern.compile(regex);
        Matcher m;
        for(int x=0; x<values.length; x++){
            m = p.matcher(values[x]);
            if(m.find())
            {
                return true;
            }
        }
        return false;
    }
    public static ArrayList<String[]> getUTWCommands(String code){
        String[] in = code.split("@");
        ArrayList<String[]> ret = new ArrayList<String[]>();
        String valid = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ -_$.";
        String current;
        int y;
        boolean quoteS;
        boolean quoteD;
        int x;
        int parens = 0;
        short z;
        String html = in[0];
        for(x=1; x<in.length; x++){
            z = 0;
            String[] out = new String[2];
            out[0] = "";
            out[1] = "";
            quoteD = false;
            quoteS = false;
            if(in[x].startsWith("ProjectUTW") && !in[x-1].trim().endsWith("#")){
                for(y=0; y<in[x].length(); y++){
                    current = in[x].substring(y, y+1);
                    if(z == 0){
                        if(!valid.contains(current)){
                            if(current.equals("(")){
                                out[0] = out[0].substring(10, out[0].length());
                                z = 1;
                                parens = 1;
                            } else{
                                html += in[x].substring(y+1);
                                out = null;
                                break;
                            }
                        } else{
                            out[0] += current;
                        }
                    } else if(out != null){
                        out[1] += current;}
                       if(current.equals("(")){
                            if(!quoteD && !quoteS){
                                parens ++;
                            }
                        } else if(current.equals(")")){
                            if(!quoteD && !quoteS){
                                parens --;
                            }
                        } else if(current.equals(";")){
                            html += in[x].substring(y+1);
                            if(parens == 1 && !quoteD && !quoteS){
                                out[1] = out[1].substring(0, out[1].length() - 2);
                                break;
                            } else{
                                out = null;
                            }
                        } else if(current.equals("\"")){
                            if(!quoteS){
                                if(quoteD){
                                    if(!in[x].substring(y-1, y).equals("\\")){
                                        quoteD = false;
                                    }
                                } else{
                                    quoteD = true;
                                }
                            }
                        } else if(current.equals("'")){
                            if(!quoteD){
                                if(quoteS){
                                    if(!in[x].substring(y-1, y).equals("\\")){
                                        quoteS = false;
                                    }
                                } else{
                                    quoteS = true;
                                }
                            }
                        }
                    }
                } else{
               html += "@" + in[x];
            }
            if(out != null){
                ret.add(out);
            }
        }
        String[] htmlL = new String[1];
        htmlL[0] = html;
        ret.add(htmlL);
        return ret;
    }
    public static boolean checkFileCanRead(File file){
        if (!file.exists()) 
            return false;
        if (!file.canRead())
            return false;
        try {
            FileReader fileReader = new FileReader(file.getAbsolutePath());
            fileReader.read();
            fileReader.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    static String parseUTWScript(String html){
        execJS.clear();
        boolean parse = false;
        HashMap<String, String> vars = new HashMap<String, String>();
        HashMap<String, Boolean> systemVars = new HashMap<String, Boolean>();
        String[] data;
        String command;
        String[] $args;
        String args;
        boolean failed;
        ArrayList<String[]> tags = getUTWCommands(html);
        String[] delete = new String[1];
        delete[0] = "false";
        int wLayers = 0;
        int wIters = 0;
        ArrayList<Integer> wLoops = new ArrayList<Integer>();
        ArrayList<String> wConds = new ArrayList<String>();
        int x;
        String _ret;
        for(x=0; x<tags.size()-1; x++){
            if(tags.get(x) != null){
                failed = false;
                data = tags.get(x);
                command = data[0];
                args = data[1];
                
                // Process command
                if(command.equals(" require")){
                    args = (String) engine.executeScript(args);
                    if(args.equals("web extensions")){
                        if(webExtensions == null){
                            if(option("This website requires you to temporarily enable web extensions.\nWould you like to?")){
                                webExtensions = new WebExtensions(engine);
                                delete[0] = "true";
                            } else{
                                JOptionPane.showMessageDialog(null, "Some features of this website may not work without web extensions.");
                            }
                        }
                    }
               } else{
                    if(webExtensions != null){
                        if(!webExtensions.runUTW(command, args, vars, systemVars)){
                            failed = true;
                        }
                    }
                }
            }
            //
        }
        if(delete[0].equals("true")){
            webExtensions = null;
        }
        return tags.get(tags.size()-1)[0];
    }
    private static void downloadFile(String url, String outputFile) throws IOException{
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }
    private static void update(String url) throws IOException, URISyntaxException{
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        if(PATH.endsWith(".jar")){
            FileOutputStream fos = new FileOutputStream(PATH);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            restartApplication();
        }
    }
    public static void restartApplication() throws URISyntaxException, IOException
    {
      startApplication();
      System.exit(0);
    }
    public static void startApplication() throws URISyntaxException, IOException
    {
      final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    
      /* is it a jar file? */
      if(!PATH.endsWith(".jar"))
        return;
    
      /* Build command: java -jar application.jar */
      final ArrayList<String> command = new ArrayList<String>();
      command.add(javaBin);
      command.add("-jar");
      command.add(PATH);
    
      final ProcessBuilder builder = new ProcessBuilder(command);
      builder.start();
    }
    public static void startApplication(String[] args) throws URISyntaxException, IOException
    {
      final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    
      /* is it a jar file? */
      if(!PATH.endsWith(".jar"))
        return;
    
      /* Build command: java -jar application.jar */
      final ArrayList<String> command = new ArrayList<String>();
      command.add(javaBin);
      command.add("-jar");
      command.add(PATH);
      for(int x=0; x<args.length; x++){
          command.add(args[x]);
      }
    
      final ProcessBuilder builder = new ProcessBuilder(command);
      builder.start();
    }
    

    public static void main(String[] args) throws IOException, URISyntaxException{
        SEARCH_ENGINES.put("duckduckgo", "https://duckduckgo.com/?q=%s");
        SEARCH_ENGINES.put("google", "https://www.google.com/search?q=%s");
        SEARCH_ENGINES.put("wikipedia", "https://en.wikipedia.org/w/index.php?searchInput=%s");
        SEARCH_ENGINE = SEARCH_ENGINES.get("duckduckgo");
            URL url = new URL("https://raw.githubusercontent.com/crash0verrid3/Project-UTW/master/version.txt.txt");
            try{
                latestVersion = (new Scanner(url.openStream())).nextInt();
            } catch(java.lang.Throwable e){
                // Ignore
            }
            if(latestVersion > ProjectUTW_VERSION){
                update("https://raw.githubusercontent.com/crash0verrid3/Project-UTW/master/JBrowser.jar");
            }
            loadPlugins();
            String load = null;
            if(args.length > 0){
                    for(int x=0; x<args.length; x++){
                        if(!args[x].startsWith("--")){
                            if(load != null){
                                startApplication(new String[]{args[x]});
                            } else{
                                load = args[x];
                            }
                        }
                    }
                }
            final String urlToLoad = load;
            load = null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                setProxy("none", false);
                browser = new SimpleSwingBrowser();
                browser.setVisible(true);
                txtURL.setPlaceholder(" Search here to get started...");
                if(urlToLoad == null){
                    browser.loadURL(HOMEPAGE);
                } else{
                    browser.loadURL(urlToLoad);
                }
            }
        });
    }
}