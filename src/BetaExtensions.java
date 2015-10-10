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

public class BetaExtensions
{
    WebEngine engine;
    public BetaExtensions(WebEngine engine){
        this.engine = engine;
    }
    public boolean runUTW(String command, String args){
        return false;
    }
}
