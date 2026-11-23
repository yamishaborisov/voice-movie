package boris.company.voicemovie.model.dto.converter;

import boris.company.voicemovie.model.InputType;
import boris.company.voicemovie.model.MovieRequestTask;
import boris.company.voicemovie.model.MovieRequestTaskStatus;

public class MovieRequestTaskConverter {
    private MovieRequestTaskConverter() {}

    public static MovieRequestTask toVoiceEntity(String fileReference) {
        MovieRequestTask task = new MovieRequestTask();
        task.setFileReference(fileReference);
        task.setInputType(InputType.VOICE);
        task.setStatus(MovieRequestTaskStatus.PENDING);
        return task;
    }

    public static MovieRequestTask toEntity(String text){
        MovieRequestTask task = new MovieRequestTask();
        task.setInputText(text);
        task.setInputType(InputType.TEXT);
        task.setStatus(MovieRequestTaskStatus.PENDING);
        return task;
    }
}
