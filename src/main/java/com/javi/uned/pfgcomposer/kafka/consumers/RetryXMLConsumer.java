package com.javi.uned.pfgcomposer.kafka.consumers;

import com.javi.uned.pfg.ScoreBuilder;
import com.javi.uned.pfg.exceptions.ExportException;
import com.javi.uned.pfg.io.Export;
import com.javi.uned.pfg.model.ScoreComposite;
import com.javi.uned.pfg.model.Specs;
import com.javi.uned.pfgcomposer.exceptions.MusescoreException;
import com.javi.uned.pfgcomposer.exceptions.SpecsConsumerException;
import com.javi.uned.pfgcomposer.kafka.producers.FileProducer;
import com.javi.uned.pfgcomposer.services.MusescoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class RetryXMLConsumer {

    private final Logger logger = LoggerFactory.getLogger(RetryXMLConsumer.class);
    private static final String TOPIC_COMPOSER_EXECUTION = "melodia.composer.retryxml";
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private FileProducer fileProducer;
    @Autowired
    private MusescoreService musescoreService;

    @KafkaListener(topics = "melodia.backend.retryxml", groupId = "0", containerFactory = "specsKafkaListenerFactory")
    public void consume(Specs specs, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        File xmlFile = null;
        File pdfFile = null;
        try{
            ScoreComposite scoreComposite = componer(specs, key);
            xmlFile = generarXML(scoreComposite, key);
            pdfFile = generarPDF(xmlFile, key);
            kafkaTemplate.send(TOPIC_COMPOSER_EXECUTION, key, "Terminado!");
            logger.info("Composición finalizada. ID={}", key);
        } catch (SpecsConsumerException e) {
            logger.error("SpecsConsumer.consume: Error al generar partitura", e);
            kafkaTemplate.send(TOPIC_COMPOSER_EXECUTION, key, e.getMessage());
        } finally { // Clean up
            try {
                if (xmlFile != null && xmlFile.exists()) Files.delete(xmlFile.toPath());
                if (pdfFile != null && pdfFile.exists()) Files.delete(pdfFile.toPath());
            } catch (IOException ioe) {
                logger.warn("No se ha podido borrar un archivo temporal", ioe);
            }
        }
    }


    private ScoreComposite componer(Specs specs, String key) {
        kafkaTemplate.send(TOPIC_COMPOSER_EXECUTION, key, "Componiendo");
        return ScoreBuilder.getInstance().buildScore(specs);
    }

    private File generarXML(ScoreComposite scoreComposite, String key) throws SpecsConsumerException {
        try{
            kafkaTemplate.send(TOPIC_COMPOSER_EXECUTION, key, "Generando fichero .musicxml");
            File xmlFile = new File(key+".musicxml");
            Export.toXML(scoreComposite, xmlFile);
            fileProducer.sendXML(key, xmlFile);
            return xmlFile;
        } catch (IOException | ExportException e) {
            throw new SpecsConsumerException("SpecsConsumer.generarXML: Error al generar XML", e);
        }
    }

    private File generarPDF(File xmlFile, String key) throws SpecsConsumerException {
        try{
            if(!xmlFile.exists()) throw new SpecsConsumerException("SpecsConsumer.generarPDF: No se ha podido crear el PDF. No existe el archivo " + xmlFile.getAbsolutePath());
            kafkaTemplate.send(TOPIC_COMPOSER_EXECUTION, key, "Generando fichero .pdf");
            File pdfFile = musescoreService.convertXMLToPDF(xmlFile, key + ".pdf");
            fileProducer.sendPDF(key, pdfFile);
            return pdfFile;
        } catch (MusescoreException e) {
            throw new SpecsConsumerException("SpecsConsumer.generarPDF: error al generar PDF", e);
        } catch (IOException e) {
            throw new SpecsConsumerException("SpecsConsumer.generarPDF: error al guardar PDF", e);
        }
    }

}
