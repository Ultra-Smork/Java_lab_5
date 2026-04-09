package com.client.handlers.script;

import com.model.Album;
import com.model.Coordinates;
import com.model.MusicBand;
import com.model.MusicGenre;

import java.util.List;

public class ScriptBandParser {
    
    private static final int EXPECTED_FIELDS = 8;
    
    public MusicBand parse(List<String> inputs) {
        if (inputs.size() < EXPECTED_FIELDS) {
            throw new IllegalArgumentException("Expected " + EXPECTED_FIELDS + " fields, got " + inputs.size());
        }
        
        MusicBand band = new MusicBand();
        
        band.setName(inputs.get(0));
        band.setNumberOfParticipants(Integer.parseInt(inputs.get(1)));
        
        String genreStr = inputs.get(2).trim();
        if (!genreStr.isEmpty()) {
            try {
                band.setGenre(MusicGenre.valueOf(genreStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                band.setGenre(null);
            }
        } else {
            band.setGenre(null);
        }
        
        long x = Long.parseLong(inputs.get(3));
        int y = Integer.parseInt(inputs.get(4));
        band.setCoordinates(new Coordinates(x, y));
        
        String description = inputs.get(5).trim();
        band.setDescription(description.isEmpty() ? null : description);
        
        String albumName = inputs.get(6);
        double sales = Double.parseDouble(inputs.get(7));
        band.setBestAlbum(new Album(albumName, sales));
        
        return band;
    }
    
    public int getExpectedFieldCount() {
        return EXPECTED_FIELDS;
    }
}