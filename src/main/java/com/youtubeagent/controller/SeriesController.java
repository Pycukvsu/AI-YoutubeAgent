package com.youtubeagent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SeriesController {

    @GetMapping("/series")
    public String seriesPage() {
        return "series";
    }
}
