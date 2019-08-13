package com.multipartfile.controllers;

import com.multipartfile.entity.Picture;
import com.multipartfile.enums.UploadMethods;
import com.multipartfile.services.implementations.PictureFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;


/**
 * @author Rayner MDZ
 */
@Controller
@RequestMapping("/picture")
public class PictureController {

  private PictureFactory factory;

  public PictureController(PictureFactory factory) {
    this.factory = factory;
  }

  @GetMapping
  @RequestMapping("/form")
  public String getUploadForm(Model model) {
    model.addAttribute("methods", UploadMethods.values());
    model.addAttribute("picture", new Picture());
    return "form";
  }

  @PostMapping
  @RequestMapping("/save-picture")
  public String savePicture(@ModelAttribute("picture") Picture picture, @RequestParam("file") MultipartFile file) {

    factory.getService(picture.getUploadMethods().toString()).saveOrUpdatePicture(picture, file);
    return "redirect:/";
  }

  @PutMapping
  @RequestMapping("/{id}/update-picture")
  public String updatePicture(@PathVariable Integer id, Model model) {
    Optional<Picture> picture = factory.getService("server").getPictureById(id);
    model.addAttribute("picture", factory.getService(picture.get().getUploadMethods().toString()).getPictureById(id).get());
    return "form";
  }

  @DeleteMapping
  @RequestMapping("/{id}/delete-picture")
  public String deletePicture(@PathVariable Integer id) {
    Optional<Picture> picture = factory.getService("server").getPictureById(id);

    picture.ifPresent(value -> factory.getService(value.getUploadMethods().toString()).deletePictureById(value.getId()));
    return "redirect:/";
  }
}
