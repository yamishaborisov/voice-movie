package boris.company.voicemovie.controller;

import boris.company.voicemovie.command.TextSearchCommand;
import boris.company.voicemovie.command.VoiceSearchCommand;
import boris.company.voicemovie.model.dto.MovieSearchResultResponse;
import boris.company.voicemovie.model.dto.request.CreateTextTaskRequest;
import boris.company.voicemovie.model.dto.response.StartMovieSearchResponse;
import boris.company.voicemovie.model.MovieRequestTask;
import boris.company.voicemovie.service.MovieTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class MovieSearchController {

    private final MovieTaskService service;

    @PostMapping("/text")
    public ResponseEntity<StartMovieSearchResponse> startTextSearch(
            @RequestBody CreateTextTaskRequest request
    ) {

        TextSearchCommand prompt = new TextSearchCommand(request.text());

        MovieRequestTask task =
                service.startMovieSearch(prompt);

        return ResponseEntity.accepted()
                .body(new StartMovieSearchResponse(task.getId()));

    }

    @PostMapping("/voice")
    public ResponseEntity<StartMovieSearchResponse> startVoiceSearch(
            @RequestParam("file") MultipartFile file
    ) {
        VoiceSearchCommand prompt = new VoiceSearchCommand(file);

        MovieRequestTask task =
                service.startMovieSearch(prompt);

        return ResponseEntity.accepted()
                .body(new StartMovieSearchResponse(task.getId()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<MovieSearchResultResponse> getSearchResult(
            @PathVariable("taskId") Long taskId
    ){
        MovieSearchResultResponse response = service.getSearchResult(taskId);
        return ResponseEntity.ok(response);
    }
}
