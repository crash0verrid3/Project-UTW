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
    public boolean runUTW(String command, String args){
        if(command.equals(" get")){
            args = (String) engine.executeScript(args);
            SimpleSwingBrowser.UTWRet = args;
        } else if(command.equals(" alert")){
            args = (String) engine.executeScript(args);
            JOptionPane.showMessageDialog(null, args);
            return true;
        } else if(command.equals("JS")){
            engine.executeScript(args);
            return true;
        } 
        return false;
    }
}
