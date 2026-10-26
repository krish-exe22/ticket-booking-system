package com.pcp.moviebooking.controller;

import com.pcp.moviebooking.dto.Dtos.UserRequest;
import com.pcp.moviebooking.service.MovieService;
import com.pcp.moviebooking.service.ShowService;
import com.pcp.moviebooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class WebController {
    private final MovieService movieService;
    private final ShowService showService;
    private final UserService userService;

    public WebController(MovieService movieService, ShowService showService, UserService userService) {
        this.movieService = movieService;
        this.showService = showService;
        this.userService = userService;
    }

    @GetMapping
    public String home(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "home";
    }

    @GetMapping("/movies/{id}")
    public String movieDetail(@PathVariable Long id, Model model) {
        model.addAttribute("movie", movieService.getMovie(id));
        model.addAttribute("shows", showService.getShowsForMovie(id));
        return "movie";
    }

    @GetMapping("/shows/{id}")
    public String showDetail(@PathVariable Long id, Model model) {
        model.addAttribute("show", showService.getShow(id));
        model.addAttribute("seats", showService.getSeats(id));
        return "show";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userRequest", new UserRequest("", ""));
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute @Valid UserRequest request, Model model) {
        try {
            userService.register(request);
            return "redirect:/login?registered=true";
        } catch (Exception exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("userRequest", request);
            return "register";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false, defaultValue = "false") boolean registered, Model model) {
        model.addAttribute("registered", registered);
        return "login";
    }
}
