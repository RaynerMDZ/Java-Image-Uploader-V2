package com.multipartfile.controllers;

import com.multipartfile.repositories.PictureRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Rayner MDZ
 */
@Controller
public class IndexController {

  private final PictureRepository repository;

  public IndexController(@Qualifier("PictureRepository") PictureRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("pictures", repository.findAll());
    return "index";
  }
}
