package org.duh102.duhbot.fortune;

import java.io.*;
import java.util.*;

import org.pircbotx.hooks.*;
import org.pircbotx.hooks.events.*;

import org.duh102.duhbot.functions.*;

public class FortunePlugin extends ListenerAdapter implements ListeningPlugin
{
  static final List<String> FORT_COMM = Arrays.asList("fortune", "-s", "fortunes");
  static final String PREFIX = ".fortune";
  
  public HashMap<String,String> getHelpFunctions()
  {
    HashMap<String,String> helpFunctions = new HashMap<String,String>();
    helpFunctions.put(PREFIX, "Responds with a fortune from the unix program 'fortune'");
    return helpFunctions;
  }
  
  public String getPluginName()
  {
    return "fortune";
  }
  
  public ListenerAdapter getAdapter()
  {
    return this;
  }

  private String getAFortune() {
    StringBuilder toRet = new StringBuilder();
    try {
      Process proc = (new ProcessBuilder(FORT_COMM)).start();
      Reader out = null;
      try {
        out = new InputStreamReader(proc.getInputStream(), "UTF-8");
      } catch (UnsupportedEncodingException uee) {
        System.err.printf("This shouldn't happen, we don't support UTF-8 encoding!");
      }
      char[] buffer = new char[1024];
      int readChars = 0;
      do {
        readChars = out.read(buffer, 0, 1024);
        if( readChars > 0 ){
          toRet.append(buffer, 0, readChars);
        }
      } while( readChars > 0 );
    } catch (IOException ioe) {
      System.err.printf("Caught exception: %s", ioe.toString());
    }
    return toRet.toString().trim().replace("\n", " ");
  }

  static String message;
  public void onMessage(MessageEvent event)
  {
    message = event.getMessage();
    if(message.startsWith(PREFIX))
    {
      event.respond(getAFortune());
    }
  }
}
