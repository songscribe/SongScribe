package songscribe.webserver;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import songscribe.converter.PDFConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

@RestController
@RequestMapping(path = "/convert")
public class WebController {
    @PostMapping(path = "/mssw-to-pdf",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public byte[] msswToPdfConverter(@RequestBody String mssw) throws Exception {
        PDFConverter pdfConverter = new PDFConverter();
        pdfConverter.paperSize = "a4";
        var file = File.createTempFile("songscribe", "pdfconverter");
        pdfConverter.files = new File[] { file };
        try (var fileWriter = new FileWriter(file)){
            fileWriter.append(mssw);
        }
        pdfConverter.convert();
        try (var pdfReader = new FileInputStream(file.getAbsolutePath() + ".pdf")){
            return pdfReader.readAllBytes();
        }
    }
}
