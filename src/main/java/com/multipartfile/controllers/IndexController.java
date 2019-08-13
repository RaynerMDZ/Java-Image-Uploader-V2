package com.multipartfile.controllers;

import com.multipartfile.services.implementations.PictureFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Rayner MDZ
 */
@Controller
public class IndexController {

  private PictureFactory factory;

  public IndexController(PictureFactory factory) {
    this.factory = factory;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("pictures", factory.getService("server").getAllPictures());
    return "index";
  }
}
