package com.multipartfile.controllers;

import com.multipartfile.entity.Picture;
import com.multipartfile.services.PictureService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Rayner MDZ
 */
@Controller
@RequestMapping("/picture")
public class PictureController {

  private PictureService service;

  public PictureController(PictureService service) {
    this.service = service;
  }

  @GetMapping("/form")
  public String getUploadForm(Model model) {
    model.addAttribute("picture", new Picture());
    return "form";
  }

  @PostMapping("/save-picture")
  public String savePicture(@ModelAttribute("picture") Picture picture, @RequestParam("file") MultipartFile file) {
    picture.setPath("");
    service.saveOrUpdatePicture(picture, file);
    return "redirect:/";
  }

  @GetMapping
  @RequestMapping("/{id}/update-picture")
  public String updatePicture(@PathVariable Integer id, Model model) {
    model.addAttribute("picture", service.getPictureById(id).get());
    return "form";
  }
}
