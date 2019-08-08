package com.multipartfile.controllers;

import com.multipartfile.entity.Picture;
import com.multipartfile.services.PictureService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Rayner MDZ
 */
@Controller
public class IndexController {

  private PictureService service;

  public IndexController(PictureService service) {
    this.service = service;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("pictures", service.getAllPictures());
    return "index";
  }
}
