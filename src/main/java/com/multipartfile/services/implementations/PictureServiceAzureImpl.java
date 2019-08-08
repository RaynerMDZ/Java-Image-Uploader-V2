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
import org.hibernate.HibernateException;
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

/**
 * @author Rayner MDZ
 */
@Service
public class PictureServiceAzureImpl implements PictureService {

  @Qualifier(value = "PictureRepository")
  private PictureRepository repository;

  @Qualifier(value = "AzureConnection")
  private AzureConnection azureConnection;

  @Qualifier(value = "Util")
  private Util util;

  public PictureServiceAzureImpl(PictureRepository repository, AzureConnection azureConnection, Util util) {
    this.repository = repository;
    this.azureConnection = azureConnection;
    this.util = util;
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
    File convertedFile;

    //Updates
    try {

      if (picture.getId() != null) {
        if (this.getPictureById(picture.getId()).isPresent()) {

          Picture foundPicture = this.getPictureById(picture.getId()).get();

          convertedFile = multipartToFile(util.getFILE_BASE_PATH(), file);

          // Parse the connection string and create a blobReference client to interact with Blob storage
          container = azureContainerConnection(azureConnection.containerName, azureConnection.storageConnectionString);

          // Create the container if it does not exist with public access.
          System.out.println("Creating container: " + container.getName());
          createContainer(container);

          //Getting a blobReference reference
          CloudBlockBlob blobReference = container.getBlockBlobReference(convertedFile.getName());

          //Creating blobReference and uploading file to it
          System.out.println("Uploading the file");
          URI = uploadFile(URI, convertedFile, blobReference);

          convertedFile.delete();
          return saveImageWithUri(foundPicture, URI);
        }
      }

    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      return Optional.empty();

    } catch (StorageException ex) {
      System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
      return Optional.empty();
    }

    //Creates
    try {

      convertedFile = multipartToFile(util.getFILE_BASE_PATH(), file);

      // Parse the connection string and create a blobReference client to interact with Blob storage
      container = azureContainerConnection(azureConnection.containerName, azureConnection.storageConnectionString);

      // Create the container if it does not exist with public access.
      System.out.println("Creating container: " + container.getName());
      createContainer(container);

      //Getting a blobReference reference
      CloudBlockBlob blobReference = container.getBlockBlobReference(convertedFile.getName());

      //Creating blobReference and uploading file to it
      System.out.println("Uploading the file");
      URI = uploadFile(URI, convertedFile, blobReference);

      convertedFile.delete();
      return saveImageWithUri(picture, URI);

    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      return Optional.empty();

    } catch (StorageException ex) {
      System.out.println(String.format("Service error. Http code: %d and error code: %s", ex.getHttpStatusCode(), ex.getErrorCode()));
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
        String[] name = picture.get().getPath().split("/");

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

      file = new File(destination + multipartFile.getOriginalFilename());
      file.createNewFile();

      multipartFile.transferTo(file);

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
   * @param id
   * @param URI
   * @return
   */
  private Optional<Picture> saveImageWithUri(Picture picture, String URI) {

    Picture foundPicture;

    // Updates
    if (picture.getId() != null) {

      if (getPictureById(picture.getId()).isPresent()) {
        foundPicture = getPictureById(picture.getId()).get();
        foundPicture.setPath(URI);
        foundPicture.setName(picture.getName());

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
      foundPicture.setPath(URI);

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
}
