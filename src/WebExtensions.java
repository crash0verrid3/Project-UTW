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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.reflect.*;

import javax.tools.*;

import netscape.javascript.JSObject;

import java.nio.channels.*;

import javax.activation.MimeType;

public class WebExtensions
{
    WebEngine engine;
    public WebExtensions(WebEngine engine){
        this.engine = engine;
    }
    private String parseUTW(String data){ // Loads and runs a UTW script
        return SimpleSwingBrowser.parseUTWScript(data);
    }
    private String loadUTW(String data){ // Loads a UTW script without running it
        ArrayList<String[]> ret = SimpleSwingBrowser.getUTWCommands(data);
        return ret.get(ret.size()-1)[0];
    }
    public boolean runUTW(String command, String args, HashMap<String, String> vars, HashMap<String, Boolean> systemVars){
        if(command.contains(".")){
            if(vars.containsKey(command)){
                if(runExtendedUTW(command, args, vars, systemVars)){
                    return true;
                } else{
                    return false;
                }
            } else{
                return false;
            }
        } else if(command.equals(" return")){
            args = (String) engine.executeScript(args);
            engine.executeScript("UTWRet = \"" + args.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace("'", "\\'") + "\";");
        } else if(command.equals(" alert")){
            args = (String) engine.executeScript(args);
            JOptionPane.showMessageDialog(null, args);
            return true;
        } else if(command.equals(" _alert")){
            JOptionPane.showMessageDialog(null, args);
            return true;
        } else if(command.equals(" JS")){
            engine.executeScript(args);
            return true;
        } else if(command.equals(" -JS")){
            engine.executeScript((String)engine.executeScript(args));
            return true;
        } else if(command.equals(" _return")){
            engine.executeScript("UTWRet = \"" + args.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace("'", "\\'") + "\";");
        }
        return false; // Did it success? true=yes, false=no
    }
    public boolean runExtendedUTW(String command, String args, HashMap<String, String> vars, HashMap<String, Boolean> systemVars){
        return false;
    }
}
