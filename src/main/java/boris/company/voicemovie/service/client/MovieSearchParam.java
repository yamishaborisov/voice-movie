package boris.company.voicemovie.service.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

//todo как будто вообще эту хуйню убрать можно
@Getter
@RequiredArgsConstructor
public enum MovieSearchParam {
    GENRE("genres.name"),
    YEAR("year"),
    RATING("rating.kp"),
    LIMIT("limit");

    private final String queryParamName;

}
