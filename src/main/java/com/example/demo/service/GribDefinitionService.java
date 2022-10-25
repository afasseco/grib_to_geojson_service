package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class GribDefinitionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GribDefinitionService.class);
    @Value("${local_table_definition_file}")
    File localGribDefinitionFile;

    public File getGribDefinition() {
        File ecmwfFormatFile = new File("cache", "local_table_2_.temp");
        if( !ecmwfFormatFile.exists()) {
            try {
                convertLocalDefinitionsToEcmwfFormat(localGribDefinitionFile, ecmwfFormatFile);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return ecmwfFormatFile;
    }

    // Function to do a quick conversion from the format that the local ec codes are in to the format that ucar grib code understands
    private void convertLocalDefinitionsToEcmwfFormat(File localGribDefinitionFile, File destination) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(localGribDefinitionFile));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(destination))
        ) {
            bufferedWriter.write("temp file for parameter names");
            bufferedWriter.write("\n");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    int i = line.indexOf(' ');
                    String num = line.substring(0, i);
                    line = line.substring(i + 1);

                    i = line.indexOf(' ');
                    String smallName = line.substring(0, i);
                    line = line.substring(i + 1);

                    i = line.indexOf(' ');
                    String name = line.substring(0, i);
                    line = line.substring(i + 1);

                    i = line.lastIndexOf(' ');
                    String description = line.substring(0, i);
                    String unit = line.substring(i + 1);

                    bufferedWriter.write("......................");
                    bufferedWriter.write("\n");
                    bufferedWriter.write(num);
                    bufferedWriter.write("\n");
                    bufferedWriter.write(name);
                    bufferedWriter.write("\n");
                    bufferedWriter.write(description);
                    bufferedWriter.write("\n");
                    bufferedWriter.write(unit);
                    bufferedWriter.write("\n");
                } catch (StringIndexOutOfBoundsException e) {
                    System.err.println("Could not parse " + line); //Last line does not conform to format, so just skip
                }
            }
        }
    }
}
