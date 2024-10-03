package org.dreamcodesoft;

import com.jcraft.jsch.SftpException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> files = new ArrayList<>();

        SftpConnection sftpConnection = new SftpConnection();
        try {
            files = sftpConnection.downloadFiles(
                    sftpConnection.getLocalDir(), sftpConnection.getRemoteDir()
            );
        } catch (SftpException e){
            System.out.println("No se ha podido descargar el archivo: " +e);
        } catch (NullPointerException e){
            System.out.println("Archivo no encontrado: " +e);
        }

        FileManager fileManager = new FileManager();


        // 2. y 3. Abrir y validar cada archivo
        for (String filePath : files) {
            List<String> errors = new ArrayList<>();
            try{
                errors = fileManager.validateExcelFile(filePath, "config.json");
            } catch (IOException e){ }
            // 4. Mostrar errores si los hay
            if (!errors.isEmpty()) {
                System.out.println("Errores encontrados en el archivo " + filePath + ":");
                for (String error : errors) {
                    System.out.println("- " + error);
                }
            } else {
                System.out.println("El archivo " + filePath + " es válido.");
            }
        }
    }
}