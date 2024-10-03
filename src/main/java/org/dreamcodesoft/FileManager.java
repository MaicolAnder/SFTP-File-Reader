package org.dreamcodesoft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public List<String> validateExcelFile(String filePath, String configPath) throws IOException {
        List<String> errors = new ArrayList<>();

        // Cargar configuración JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode config;

        try (InputStream configInput = getClass().getClassLoader().getResourceAsStream(configPath)) {
            if (configInput == null) {
                throw new IOException("No se pudo encontrar el recurso: " + configPath);
            }
            config = mapper.readTree(configInput);
        }

        // Abrir archivo Excel
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            // Validar encabezados
            JsonNode headers = config.get("headers");
            for (JsonNode header : headers) {
                String expectedHeader = header.asText();
                boolean found = false;
                for (Cell cell : headerRow) {
                    if (cell.getStringCellValue().equals(expectedHeader)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    errors.add("Encabezado faltante: " + expectedHeader);
                }
            }

            // Validar campos obligatorios
            JsonNode requiredFields = config.get("requiredFields");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                for (JsonNode field : requiredFields) {
                    int columnIndex = field.get("columnIndex").asInt();
                    Cell cell = row.getCell(columnIndex);
                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        errors.add("Campo obligatorio vacío en fila " + (i + 1) + ", columna " + (columnIndex + 1));
                    }
                }
            }
        }

        return errors;
    }
}
