package com.multipartfile.services.implementations;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.multipartfile.entity.Picture;
import com.multipartfile.repositories.PictureRepository;
import com.multipartfile.services.PictureService;
import com.multipartfile.util.AzureConnection;
import com.multipartfile.util.Util;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author Rayner MDZ
 */
@Service
//@Profile("azure")
public class PictureServiceAzureImpl implements PictureService {

  @Qualifier(value = "PictureRepository")
  private final PictureRepository repository;

  @Qualifier(value = "AzureConnection")
  private final AzureConnection azureConnection;

  @Qualifier(value = "Util")
  private final Util util;

  private final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public PictureServiceAzureImpl(PictureRepository repository, AzureConnection azureConnection, Util util) {
    log.info("Azure service loaded!");
    this.repository = repository;
    this.azureConnection = azureConnection;
    this.util = util;
  }

  @Override
  public String getType() {
    return "azure";
  }

  /**
   *
   * @return
   */
  @Override
  public Iterable<Picture> getAllPictures() {
    return this.repository.findAll();
  }

  /**
   *
   * @param id
   * @return
   */
  @Override
  public Optional<Picture> getPictureById(Integer id) {
    return this.repository.findById(id);
  }

  /**
   *
   * @param picture
   * @param file
   * @return
   */
  @Override
  public Optional<Picture> saveOrUpdatePicture(Picture picture, MultipartFile file) {

    CloudBlobContainer container;
    String URI = "";
    File convertedFile = null;

    //Updates
    try {
      if (picture.getId() != null) {

        log.info("Updating file");

        if (this.getPictureById(picture.getId()).isPresent()) {

          Picture foundPicture = this.getPictureById(picture.getId()).get();

          convertedFile = multipartToFile(util.getFILE_BASE_PATH(), file);

          // Parse the connection string and create a blobReference client to interact with Blob storage
          container = azureContainerConnection(azureConnection.containerName, azureConnection.storageConnectionString);

          // Create the container if it does not exist with public access.
          createContainer(container);

          boolean success = deleteImageFromStorage(picture, container);

          //Getting a blobReference reference
          CloudBlockBlob blobReference = container.getBlockBlobReference(convertedFile.getName());

          //Creating blobReference and uploading file to it
          URI = uploadFile(URI, convertedFile, blobReference);

          convertedFile.delete();

          log.info("Updating file done!");

          return saveImageWithUri(foundPicture, URI);
        }
      }

    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      convertedFile.delete();
      return Optional.empty();

    } catch (StorageException ex) {
      System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
      convertedFile.delete();
      return Optional.empty();
    }

    //Creates
    try {

      log.info("Creating file");

      convertedFile = multipartToFile(util.getFILE_BASE_PATH(), file);

      // Parse the connection string and create a blobReference client to interact with Blob storage
      container = azureContainerConnection(azureConnection.containerName, azureConnection.storageConnectionString);

      // Create the container if it does not exist with public access.
      createContainer(container);

      //Getting a blobReference reference
      CloudBlockBlob blobReference = container.getBlockBlobReference(convertedFile.getName());

      //Creating blobReference and uploading file to it
      URI = uploadFile(URI, convertedFile, blobReference);

      convertedFile.delete();

      log.info("Creating file done!");

      return saveImageWithUri(picture, URI);

    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      convertedFile.delete();
      return Optional.empty();

    } catch (StorageException ex) {
      System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
      convertedFile.delete();
      return Optional.empty();
    }
  }

  /**
   *
   * @param id
   * @return
   */
  @Override
  public boolean deletePictureById(Integer id) {

    Optional<Picture> picture = getPictureById(id);
    CloudBlobContainer container;

    if (picture.isPresent()) {

      try {

        container = azureContainerConnection(azureConnection.containerName, azureConnection.storageConnectionString);

        if (container == null) {
          return false;
        }

        // Separates the URI into an array.
        String[] name = picture.get().getUrl().split("/");

        // Gets the last element in the array. This will be the name of the blob.
        // Looks for that name inside the container.
        CloudBlockBlob blob = container.getBlockBlobReference(name[name.length-1]);

        if (blob.exists()) {

          blob.delete();
          System.out.println("Blob with name: " + name[name.length-1] + " Deleted!");

        } else {
          System.out.println("Blob with name " + name[name.length -1] + " does not exist");
        }

        repository.delete(picture.get());

        return true;

      } catch (URISyntaxException e) {
        e.printStackTrace();

      } catch (StorageException ex) {
        System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
      }
      return false;
    }

    return false;
  }

  private boolean deleteImageFromStorage(Picture picture, CloudBlobContainer container) {

    try {

      // Separates the URI into an array.
      String[] name = picture.getUrl().split("/");

      // Gets the last element in the array. This will be the name of the blob.
      // Looks for that name inside the container.
      CloudBlockBlob blob = container.getBlockBlobReference(name[name.length-1]);

      if (blob.exists()) {

        blob.delete();

        log.info("Blob with name: " + name[name.length-1] + " Deleted!");

        return true;
      } else {
        log.info("Blob with name " + name[name.length -1] + " does not exist");
        return false;
      }

    } catch (URISyntaxException e) {
      e.printStackTrace();
      return false;

    } catch (StorageException ex) {
      System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
      return false;
    }

  }

  /**
   *
   * @param azureContainerName
   * @param azureStorageConnectionString
   * @return
   */
  private CloudBlobContainer azureContainerConnection(String azureContainerName, String azureStorageConnectionString)  {

    CloudStorageAccount storageAccount;
    CloudBlobClient blobClient;
    CloudBlobContainer container;

    try {

      storageAccount = CloudStorageAccount.parse(azureStorageConnectionString);
      blobClient = storageAccount.createCloudBlobClient();
      container = blobClient.getContainerReference(azureContainerName);
      return container;

    } catch (URISyntaxException | InvalidKeyException e) {
      e.printStackTrace();

    } catch (StorageException ex) {
      System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
    }
    return null;
  }

  /**
   *
   * @param destination
   * @param multipartFile
   * @return
   */
  private File multipartToFile(String destination, MultipartFile multipartFile) {

    File file;

    try {

      file = new File(destination + generateString() + getFileExtension(multipartFile.getOriginalFilename()));
      boolean created = file.createNewFile();

      if (created) multipartFile.transferTo(file.getAbsoluteFile());

      return file;

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   *
   * @param URI
   * @param newFile
   * @param blob
   * @return
   * @throws StorageException
   * @throws IOException
   */
  private String uploadFile(String URI, File newFile, CloudBlockBlob blob) throws StorageException, IOException {
    blob.uploadFromFile(newFile.getAbsolutePath());
    URI = blob.getUri().toString();
    return URI;
  }

  /**
   *
   * @param container
   * @throws StorageException
   */
  private void createContainer(CloudBlobContainer container) throws StorageException {
    container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());
  }

  /**
   *
   * @param picture
   * @param URI
   * @return
   */
  private Optional<Picture> saveImageWithUri(Picture picture, String URI) {

    Picture foundPicture;

    // Updates
    if (picture.getId() != null) {

      if (getPictureById(picture.getId()).isPresent()) {
        foundPicture = getPictureById(picture.getId()).get();
        foundPicture.setUrl(URI);
        foundPicture.setName(picture.getName());
        foundPicture.setUploadMethods(picture.getUploadMethods());

        try {
          return Optional.of(repository.save(foundPicture));

        } catch (DataException e) {
          e.printStackTrace();
          return Optional.empty();
        }
      }
    }

    // Creates
    try {
      foundPicture = new Picture();
      foundPicture.setName(picture.getName());
      foundPicture.setUrl(URI);
      foundPicture.setUploadMethods(picture.getUploadMethods());

      repository.save(foundPicture);
      return Optional.of(repository.save(foundPicture));

    } catch (DataException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  /**
   * Creates a random name.
   * @return a random generated string.
   */
  public String generateString() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * Removes all characters before the last 'DOT' from the name.
   * @param name as the file name
   * @return the extension of the file.
   */
  public String getFileExtension(String name) {

    String extension;
    try {
      extension = name.substring(name.lastIndexOf("."));

    } catch (Exception e) {
      extension = "";
    }
    return extension;
  }
}
