package boris.company.voicemovie.scheduler;

import boris.company.voicemovie.service.processor.MovieRequestTaskProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovieRequestTaskScheduler {

    private final MovieRequestTaskProcessor processor;

    @Scheduled(fixedDelayString = "${scheduler.movie-request.fixed-delay}")
    public void processPendingTasks() {
        processor.process();
    }

}
