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
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

import netscape.javascript.JSObject;

import java.nio.channels.*;

import javax.activation.MimeType;

import static javafx.concurrent.Worker.State.FAILED;
  
public class SimpleSwingBrowser extends JFrame {
 
    private final JFXPanel jfxPanel = new JFXPanel();
    private static WebEngine engine;
    
    private static String[] proxy = new String[2];
 
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel lblStatus = new JLabel();


    private final JButton btnGo = new JButton("Go");
    private static final JTextField txtURL = new JTextField();
    private final JProgressBar progressBar = new JProgressBar();
    
    private static SimpleSwingBrowser browser;
    
    private static JSCommands jsCommands;
    
 
    public SimpleSwingBrowser() {
        super();
        initComponents();
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
 
        btnGo.addActionListener(al);
        txtURL.addActionListener(al);
  
        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);
  
        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);
 
        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);
 
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);
        
        getContentPane().add(panel);
        
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
                                    JSObject win = (JSObject) engine.executeScript("window");
                                    win.setMember("ProjectUTW", jsCommands);
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
                    String tmp = toURL(url2);
     
                    if (tmp == null) {
                        tmp = toURL("http://" + url2);
                    }
     
                    engine.load(tmp);
                } else if(url.startsWith("get:")){
                    getAttrib(url.substring(4).trim());
                } else{
                    String tmp = toURL(url);
     
                    if (tmp == null) {
                        tmp = toURL("http://" + url);
                    }
     
                    engine.load(tmp);
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
        } else if(attrib.toLowerCase().equals("real ip")){
            getStunIP();
        } else if(attrib.toLowerCase().equals("javascript")){
            getJSSupport();
        } else if(attrib.toLowerCase().equals("iframes")){
            getIframeSupport();
        } else if(attrib.toLowerCase().equals("project")){
            browser.loadURL("http://crash0verrid3.github.io/Project-UTW/");
        } else if(attrib.toLowerCase().equals("welcome")){
            engine.loadContent("<!DOCTYPE html>\n<html>\n<head>\n<title>Project UTW</title>\n</head>\n<body>\n<h1>Welcome to the Project UTW browser</h1>\n<h3><strong><a href=\"https://duckduckgo.com/\">Click here</a></strong> to search the web.</h3>\n<p><a href=\"http://crash0verrid3.github.io/Project-UTW/\">View Project UTW on Github</a></p>\n<p>Project UTW is an open-source browser written by <strong>Alex Anderson</strong> using only the Java programming language.</p>\n<p>For a tutorial on using this browser, just type \"<strong><em>get: tutorial</em></strong>\" into the URL bar.<strong><em><br /></em></strong></p>\n<p>This browser will never keep any permanant history from your browsing,</p>\n<p>and is designed for easy use with a <em><a href=\"https://en.wikipedia.org/wiki/Proxy_server#Types_of_proxy\">web proxy</a></em>.</p>\n<p>&nbsp;</p>\n<p>At any time, you can type into the URL bar \"proxy: [proxy ip:port goes here]\"</p>\n<p>and the browser will use that proxy. Note, the proxy will not be saved for use</p>\n<p>after you close the browser. You can also type instead of the ip:port of the proxy:</p>\n<ul>\n<li>\"none\" - Restores the browser to not using a proxy</li>\n<li>\"default\" - Uses a preconfigured proxy server.</li>\n</ul>\n<p>To get the current proxy, type \"get: proxy\" into the URL bar.</p>\n</body>\n</html>\n");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setProxy("none", false);
                browser = new SimpleSwingBrowser();
                browser.setVisible(true);
                jsCommands = new JSCommands(engine, browser);
                String homepage = "get: Welcome";
                browser.loadURL(homepage);
           }     
       });
    }
}