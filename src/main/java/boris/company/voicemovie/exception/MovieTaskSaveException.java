package boris.company.voicemovie.exception;

public class MovieTaskSaveException extends RuntimeException {

    public MovieTaskSaveException(Throwable cause) {
        super("Не удалось сохранить задачу поиска фильма", cause);
    }
}
