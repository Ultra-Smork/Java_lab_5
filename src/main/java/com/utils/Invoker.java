package com.utils;

import com.commands.AddElementCommand;
import com.commands.AddIfMinCommand;
import com.commands.CleanHeapCommand;
import com.commands.ExitCommand;
import com.commands.ExecuteScriptCommand;
import com.commands.HelpCommand;
import com.commands.ListEverythingCommand;
import com.commands.ListInfoCommand;
import com.commands.RemoveByIdCommand;
import com.commands.RemoveByBestAlbumCommand;
import com.commands.RemoveGreaterThanIdCommand;
import com.commands.ShowHistoryCommand;
import com.commands.UpdateElementCommand;


public class Invoker {
    ListEverythingCommand listEverythingCommand = new ListEverythingCommand();
    ExitCommand exitCommand = new ExitCommand();
    ListInfoCommand listInfoCommand = new ListInfoCommand();
    AddElementCommand addElementCommand = new AddElementCommand();
    AddIfMinCommand addIfMinCommand = new AddIfMinCommand();
    ShowHistoryCommand printHistoryCommand = new ShowHistoryCommand();
    HelpCommand helpCommand = new HelpCommand();
    CleanHeapCommand cleanHeapCommand = new CleanHeapCommand();
    RemoveByIdCommand removeByIdCommand = new RemoveByIdCommand();
    RemoveByBestAlbumCommand removeByBestAlbumCommand = new RemoveByBestAlbumCommand();
    RemoveGreaterThanIdCommand removeGreaterThanIdCommand = new RemoveGreaterThanIdCommand();
    UpdateElementCommand updateElementCommand = new UpdateElementCommand();
    ExecuteScriptCommand executeScriptCommand;

    public void RemoveById(Long id){
        removeByIdCommand.executeWithInt(id);
    }
    public void RemoveByBestAlbum(String albumName){
        removeByBestAlbumCommand.executeWithString(albumName);
    }
    public void RemoveGreaterThanId(Long id){
        removeGreaterThanIdCommand.executeWithLong(id);
    }
    public void UpdateElement(){
        updateElementCommand.execute();
    }
    public void ListAll(){
        listEverythingCommand.execute();
    }
    public void Exit(){
        exitCommand.execute();
    }
    public void Clear(){
        cleanHeapCommand.execute();
    }
    public void ListInfo(){
        listInfoCommand.execute();
    }
    public void AddElement(){
        addElementCommand.execute();
    }

    public void AddIfMin(){
        addIfMinCommand.execute();
    }

    public void PrintHistory(){
        printHistoryCommand.execute();
    }

    public void PrintHelp(){
        helpCommand.execute();
    }

    public void ExecuteScript(String filePath){
        executeScriptCommand = new ExecuteScriptCommand(filePath);
        executeScriptCommand.setInvoker(this);
        executeScriptCommand.execute();
    }

    public void UpdateElementWithId(Long id){
        updateElementCommand.executeWithId(id);
    }
}
