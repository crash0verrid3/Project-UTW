import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
 
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
    public JSCommands(WebEngine engine){
        this.engine = engine;
    }
    public void back(){
        JSObject history = (JSObject) engine.executeScript("history");
        history.call("back");
    }
    public void test(){
        JOptionPane.showMessageDialog(null, "Hi!");
    }
}
