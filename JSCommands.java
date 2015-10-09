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

/**
 * Write a description of class JSCommands here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class JSCommands
{
    private WebEngine engine;
    private SimpleSwingBrowser browser;
    
    public JSCommands(WebEngine engine, SimpleSwingBrowser browser){
        this.engine = engine;
        this.browser = browser;
    }
    
    public void test(){
        JOptionPane.showMessageDialog(null, "Hello!");
    }
}
