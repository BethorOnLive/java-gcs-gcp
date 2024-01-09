package com.example.demo;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

@Component
public class AdminStorage implements CommandLineRunner {
	// The ID of your GCP project
	String projectId = "sample-project-407519";
	// The ID of your GCS bucket
	String bucketName = "sample-project-bucket01";
	// The path of the local directory
	String sourceDirectory = "/home/computadora/18DIC2018";


	@Override
	public void run(String... args) throws Exception {
		// Agrega aquí el código que deseas ejecutar al iniciar la aplicación
		System.out.println("La aplicación se ha iniciado y esta clase se ejecuta automáticamente.");
		uploadDirectory(projectId,bucketName,sourceDirectory);
	}

	public static void uploadDirectory(String projectId, String bucketName, String sourceDirectory) {
		// Se crea un objeto Storage para interactuar con Google Cloud Storage
		Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

		try {
			// Se obtiene la ruta del directorio local
			Path sourceDirectoryPath = Paths.get(sourceDirectory);

			// Se utiliza Files.walkFileTree para recorrer archivos y subdirectorios del directorio local
			Files.walkFileTree(sourceDirectoryPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new FileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					return FileVisitResult.CONTINUE;
				}

				// En el método visitFile se realiza la operación al visitar cada archivo
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					// Se obtiene el nombre relativo del archivo respecto al directorio origen
					String objectName = sourceDirectoryPath.relativize(file.toAbsolutePath()).toString();

					// Se crea un BlobId para identificar el objeto en el bucket de Google Cloud Storage
					BlobId blobId = BlobId.of(bucketName, objectName);

					// Se construye la información del blob
					BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

					try {
						// Se sube el contenido del archivo al bucket
						storage.create(blobInfo, Files.readAllBytes(file));
						System.out.println("File " + file.toString() + " uploaded to " + blobId);
					} catch (IOException e) {
						System.err.println("Failed to upload file: " + file.toString() + ", " + e.getMessage());
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					System.err.println("Failed to visit file: " + file.toString() + ", " + exc.getMessage());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			System.err.println("Error walking through the directory: " + e.getMessage());
		}
	}

}
