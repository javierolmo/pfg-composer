package com.javi.uned.pfgcomposer.services;

import com.javi.uned.pfgcomposer.exceptions.MusescoreException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class MusescoreService {

    private final Logger logger = LoggerFactory.getLogger(MusescoreService.class);
    private String musescoreName;

    public MusescoreService() {
        try {
            this.musescoreName = getMusescoreName();
            logger.info("Localizada instalación de MuseScore: {}", this.musescoreName);
        } catch (IOException e) {
            logger.error("Error al inicializar MusescoreService: ", e);
        }

    }

    private String getMusescoreName() throws IOException {
        try{
            String[] possibleNames = new String[]{"mscore", "mscore3"};
            for (String possibleName : possibleNames) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("sh", "-c", "command -v "+possibleName);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if(exitCode == 0) return possibleName;
            }
            throw new FileNotFoundException("No se ha encontrado una instalación válida de Musescore");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FileNotFoundException("Interrupción al encontrar una instalación válida de Musescore");
        }
    }

    public File convertXMLToPDF(File xmlFile, String pdfPath) throws MusescoreException {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh", "-c", String.format("%s %s -o %s", this.musescoreName, xmlFile.getAbsolutePath(), pdfPath));
            Process process = builder.start();
            int exitCode = process.waitFor();
            File pdfFile = new File(pdfPath);
            if (exitCode != 0 || !pdfFile.exists()) {
                throw new MusescoreException("Error al convertir el archivo '" + xmlFile.getName() + "'");
            } else {
                return pdfFile;
            }
        } catch (IOException e) {
            throw new MusescoreException("Error al convertir el archivo '" + xmlFile.getName() + "'", e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MusescoreException("Interrupción al convertir el archivo '" + xmlFile.getName() + "'", ie);
        }
    }
}
