package com.javi.uned.pfgcomposer.kafka.consumers;

import com.javi.uned.pfgcomposer.exceptions.MusescoreException;
import com.javi.uned.pfgcomposer.exceptions.SpecsConsumerException;
import com.javi.uned.pfgcomposer.kafka.producers.FileProducer;
import com.javi.uned.pfgcomposer.services.MusescoreService;
import org.apache.commons.io.FileUtils;
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
public class RetryPDFConsumer {

    private final Logger logger = LoggerFactory.getLogger(RetryPDFConsumer.class);
    private static final String TOPIC_COMPOSER_EXECUTION = "melodia.composer.retrypdf";
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private MusescoreService musescoreService;
    @Autowired
    private FileProducer fileProducer;

    @KafkaListener(topics = "melodia.backend.retrypdf", groupId = "0", containerFactory = "retryPDFListenerFactory")
    public void consume(byte[] filebinary, @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        File xmlFile = new File(key + ".musicxml");
        File pdfFile = null;
        try{
            FileUtils.writeByteArrayToFile(xmlFile, filebinary);
            pdfFile = generarPDF(xmlFile, key);
            kafkaTemplate.send(TOPIC_COMPOSER_EXECUTION, key, "Terminado!");
            logger.info("Composición finalizada. ID={}", key);
        } catch (SpecsConsumerException e) {
            logger.error("SpecsConsumer:consume -> Error al generar partitura", e);
            kafkaTemplate.send(TOPIC_COMPOSER_EXECUTION, key, e.getMessage());
        } catch (IOException e) {
            logger.error("SpecsConsumer:consume -> Error al deserializar musicxml", e);
            kafkaTemplate.send(TOPIC_COMPOSER_EXECUTION, key, e.getMessage());
        } finally { // Clean up
            try {
                if (xmlFile.exists()) Files.delete(xmlFile.toPath());
                if (pdfFile != null && pdfFile.exists()) Files.delete(pdfFile.toPath());
            } catch (IOException ioe) {
                logger.warn("No se ha podido borrar un archivo temporal", ioe);
            }
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
