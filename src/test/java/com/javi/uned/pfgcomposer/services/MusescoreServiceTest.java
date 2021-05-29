package com.javi.uned.pfgcomposer.services;

import com.javi.uned.pfg.ScoreBuilder;
import com.javi.uned.pfg.exceptions.ExportException;
import com.javi.uned.pfg.io.Export;
import com.javi.uned.pfg.model.Instrumento;
import com.javi.uned.pfg.model.ScoreComposite;
import com.javi.uned.pfg.model.Specs;
import com.javi.uned.pfg.model.constants.Instrumentos;
import com.javi.uned.pfgcomposer.exceptions.MusescoreException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MusescoreServiceTest {

    @Autowired
    private MusescoreService musescoreService;
    private File fileXML = new File("test.musicxml");
    private File filePDF = new File("test.pdf");

    @BeforeAll
    void setUp() throws ExportException {
        Specs specs = new Specs();
        specs.setMeasures(1);
        specs.setInstrumentos(new Instrumento[]{Instrumentos.VIOLIN});
        specs.setAuthors(Arrays.asList("Javier Olmo Injerto"));
        ScoreComposite scoreComposite = ScoreBuilder.getInstance().buildScore(specs);
        Export.toXML(scoreComposite, fileXML);
    }

    @Test
    void FileExport_XMLToPDF_Success() throws MusescoreException {
        musescoreService.convertXMLToPDF(fileXML, filePDF.getAbsolutePath());
        assert filePDF.exists();
    }

    @Test
    void FileExport_XMLToPDF_MissingXML() {
        try{
            File notExistingFile = new File("notexistingfile.musicxml");
            assert !notExistingFile.exists();
            musescoreService.convertXMLToPDF(notExistingFile, filePDF.getAbsolutePath());
        } catch (MusescoreException me) {
            assert true;
        }
    }

    @AfterAll
    void tearDown() {
        assert fileXML.delete();
        assert filePDF.delete();
    }

}