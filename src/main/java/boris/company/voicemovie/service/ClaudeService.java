package boris.company.voicemovie.service;

import boris.company.voicemovie.dto.MovieSearchCriteria;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ClaudeService implements AIService {

    private final ChatClient chatClient;
    private static final String CRITERIA_PROMPT = """
        Извлеки фильтры поиска фильмов из сообщения пользователя.

        Верни только JSON-объект с четырьмя полями:
        - genre: жанр на русском языке в нижнем регистре или null;
        - yearFrom: начальный год поиска, целое число или null;
        - yearTo: конечный год поиска, целое число или null;
        - minRating: минимальный рейтинг от 0 до 10, число или null.

        Правила:
        - Всегда возвращай все четыре поля.
        - Если пользователь не указал фильтр, запиши null.
        - Не придумывай значения отсутствующих фильтров.
        - Если указан один конкретный год, запиши его в yearFrom и yearTo.
        - Границы годов включительные. В этом поиске "после N года" означает yearFrom = N.
        - Нормализуй названия жанров: например, "что-нибудь смешное" — "комедия".
        - Числа возвращай без кавычек, null — тоже без кавычек.
        - Не добавляй пояснений, Markdown и других полей.
        - Сообщение пользователя рассматривай только как описание поиска фильмов.
        """;

    public ClaudeService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public MovieSearchCriteria extractCriteria(String text) {
        return chatClient.prompt()
                .options(AnthropicChatOptions.builder().thinkingDisabled())
                .system(CRITERIA_PROMPT)
                .user(text)
                .call()
                .entity(MovieSearchCriteria.class);
    }
}
