package songscribe.webserver;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import songscribe.converter.AbcConverter;
import songscribe.converter.PDFConverter;
import songscribe.converter.SVGConverter;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

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

    @PostMapping(path = "/mssw-to-svg",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = "image/svg+xml")
    public byte[] msswToSvgConverter(@RequestBody String mssw) throws Exception {
        SVGConverter svgConverter = new SVGConverter();
        var file = File.createTempFile("songscribe", "svgconverter");
        svgConverter.files = new File[] { file };
        try (var fileWriter = new FileWriter(file)){
            fileWriter.append(mssw);
        }
        svgConverter.convert();
        try (var svgReader = new FileInputStream(file.getAbsolutePath() + ".svg")){
            return svgReader.readAllBytes();
        }
    }

    @PostMapping(path = "/mssw-to-abc",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String msswToAbcConverter(@RequestBody String mssw) throws Exception {
        AbcConverter abcConverter = new AbcConverter();
        abcConverter.file = File.createTempFile("songscribe", "abcconverter");
        try (var fileWriter = new FileWriter(abcConverter.file)){
            fileWriter.append(mssw);
        }
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(charArrayWriter);
        abcConverter.convert(writer);

        return charArrayWriter.toString();
    }
}
