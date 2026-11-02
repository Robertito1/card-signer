package com.bptn.project;

import java.nio.file.Path;

import com.bptn.project.export.CardExporter;
import com.bptn.project.io.ConsoleInput;
import com.bptn.project.io.InputProvider;
import com.bptn.project.model.template.TemplateCatalog;
import com.bptn.project.render.AsciiCardRenderer;
import com.bptn.project.render.CardRenderer;
import com.bptn.project.repository.CardRepository;
import com.bptn.project.repository.FileCardRepository;
import com.bptn.project.ui.Menu;

/**
 * Local Birthday Card & Message Signer - offline greeting card utility.
 * Creates ASCII-bordered cards, collects signatures from many people, and
 * exports finished cards to .txt files.
 */
public class App {

    public static void main(String[] args) {
    	//Repository is instantiated using the Path /data/cards/ as the data store	
        CardRepository repository = new FileCardRepository(Path.of("data", "cards"));
        
        CardRenderer renderer = new AsciiCardRenderer();
        
        // Exports Object instantiated using the /exports folder as the location to store .txt cards
        CardExporter exporter = new CardExporter(Path.of("exports"), renderer);
        
        //Single input is used for the entire application
        InputProvider input = new ConsoleInput(System.in, System.out);
        
        //Menu is instantiated with all the required dependencies
        Menu menu = new Menu(
            repository, renderer, exporter, input, System.out, TemplateCatalog.all());
        menu.run();
    }
}

