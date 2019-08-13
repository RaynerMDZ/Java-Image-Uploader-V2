package com.multipartfile.services.implementations;

import com.multipartfile.entity.Picture;
import com.multipartfile.repositories.PictureRepository;
import com.multipartfile.services.PictureService;
import com.multipartfile.util.Util;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Rayner MDZ
 */
@Service
//@Profile("server")
public class PictureServiceServerImpl implements PictureService {

  @Qualifier(value = "PictureRepository")
  private final PictureRepository repository;

  @Qualifier(value = "Util")
  private final Util util;

  private final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public PictureServiceServerImpl(PictureRepository repository, Util util) {
    log.info("Server service loaded!");
    this.repository = repository;
    this.util = util;
  }

  @Override
  public String getType() {
    return "server";
  }

  /**
   *
   * @return
   */
  @Override
  public Iterable<Picture> getAllPictures() {
    log.info("Getting all pictures!");
    return this.repository.findAll();
  }

  /**
   * Gets a picture by its id from the database.
   * @param id
   * @return a picture object.
   */
  @Override
  public Optional<Picture> getPictureById(Integer id) {
    log.info("Getting picture with the id of " + id);
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

    File createdFile;

    try {

      if (picture.getId() != null) {

        Optional<Picture> foundPicture = this.getPictureById(picture.getId());

        // update
        if (foundPicture.isPresent()) {

          log.info("Updating the picture with the id of " + foundPicture.get().getId());

          if (file != null) {

            if (deletePictureFromServer(util.getFILE_BASE_PATH() + foundPicture.get().getPath())) {

              createdFile = new File(util.getFILE_BASE_PATH() + file.getOriginalFilename());
              createdFile.createNewFile();

              log.info("File created!");

              file.transferTo(createdFile.getAbsoluteFile());
              foundPicture.get().setPath(file.getOriginalFilename());

              log.info("Update done!");

              return Optional.of(repository.save(foundPicture.get()));
            }
          }
        }
      }

      // create
      if (file != null) {

        log.info("Saving new picture!");

        createdFile = new File(util.getFILE_BASE_PATH() + file.getOriginalFilename());
        createdFile.createNewFile();

        file.transferTo(createdFile.getAbsoluteFile());
        picture.setPath(file.getOriginalFilename());
      }

      log.info("Picture created!");

      return Optional.of(repository.save(picture));

    } catch (IOException e) {
      e.printStackTrace();
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

    log.info("Deleting picture from server!");

    boolean success = false;
    Optional<Picture> picture;

    try {

      picture = this.getPictureById(id);

      if (picture.isPresent()) {
        log.info("Picture is present.");
        success = deletePictureFromServer(util.getFILE_BASE_PATH() + picture.get().getPath());
        log.info("Picture deleted from server!");
      }

      if (success) {
        this.repository.deleteById(id);
        log.info("Picture deleted from server!");
        return true;
      }
      return false;

    } catch (HibernateException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Deletes a picture from the server.
   * @param path as the path of the file
   * @return a boolean
   */
  private boolean deletePictureFromServer(String path) {
    File foundFile;
    foundFile = new File(path);
    return foundFile.delete();
  }
}
